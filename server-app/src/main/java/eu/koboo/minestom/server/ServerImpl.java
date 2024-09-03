package eu.koboo.minestom.server;

import eu.koboo.minestom.api.config.ServerConfig;
import eu.koboo.minestom.api.server.Server;
import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import eu.koboo.minestom.api.world.manager.WorldManager;
import eu.koboo.minestom.commands.CommandStop;
import eu.koboo.minestom.commands.CommandVersion;
import eu.koboo.minestom.commands.CommandWorld;
import eu.koboo.minestom.config.ConfigLoader;
import eu.koboo.minestom.console.Console;
import eu.koboo.minestom.server.world.WorldManagerImpl;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ServerImpl extends Server {

    public static boolean DEBUG = false;

    @Getter
    @NonFinal
    static ServerImpl instance;

    ServerConfig serverConfig;

    WorldManagerImpl worldManager;

    @Getter
    Console console;

    public ServerImpl(String[] args) {
        super(args);

        if (Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--debug"))) {
            Logger.info("Debug mode enabled!");
            DEBUG = true;
        }
        long startTime = System.nanoTime();

        instance = this;

        Logger.info("Loading settings..");
        serverConfig = ConfigLoader.loadConfig();

        Logger.info("Initializing console..");
        console = new Console();

        Logger.info("Initializing world manager..");
        worldManager = new WorldManagerImpl();



        Logger.info("Initializing server..");
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getExceptionManager()
                .setExceptionHandler(exc -> Logger.error("An unexpected error occurred! ", exc));

        Logger.info("Registering commands..");
        MinecraftServer.getCommandManager().register(new CommandStop());
        MinecraftServer.getCommandManager().register(new CommandVersion());
        MinecraftServer.getCommandManager().register(new CommandWorld());

        String host = serverConfig.host();
        int port = serverConfig.port();

        MinecraftServer.setBrandName(this.getName());
        MinecraftServer.setDifficulty(serverConfig.difficulty());

        MinecraftServer.setCompressionThreshold(serverConfig.compressionThreshold());

        setViewDistance("minestom.chunk-view-distance", serverConfig.chunkViewDistance());
        setViewDistance("minestom.entity-view-distance", serverConfig.entityViewDistance());

        switch (serverConfig.proxyMode()) {
            case NONE -> {
                if (serverConfig.onlineMode()) {
                    MojangAuth.init();
                    Logger.info("ProxyMode 'NONE', enabled MojangAuth.");
                } else {
                    if (Arrays.stream(args).noneMatch(s -> s.equalsIgnoreCase("--cracked"))) {
                        Logger.warn("ProxyMode 'NONE', without MojangAuth.");
                        Logger.warn("WARNING: This is not recommended, as it allows cracked clients to join!");
                        Logger.warn("WARNING: Please enable MojangAuth or use a proxy, unless you know what you are doing!");
                        Logger.warn("To disable this warning, start the server with --cracked");
                    }
                }
            }
            case VELOCITY -> {
                if (serverConfig.velocitySecret() == null || serverConfig.velocitySecret().equalsIgnoreCase("")) {
                    Logger.warn("ProxyMode 'VELOCITY' selected, but no proxy-secret set! Abort!");
                    System.exit(0);
                    break;
                }
                VelocityProxy.enable(serverConfig.velocitySecret());
                Logger.info("ProxyMode 'VELOCITY', enabled VelocityProxy.");
            }
            case BUNGEECORD -> {
                BungeeCordProxy.enable();
                Logger.info("ProxyMode 'BUNGEECORD', enabled BungeeCordProxy.");
            }
        }
        Logger.info("Starting @ " + host + ":" + port);

        setupDefaultWorld();

        Logger.info("Loading worlds..");
        worldManager.loadAllAvailableWorlds();

        minecraftServer.start(host, port);
        Logger.info("Listening on " + host + ":" + port);

        console.start();

        MinecraftServer.getSchedulerManager().buildShutdownTask(buildShutdownTask());
        if (DEBUG) {
            Logger.info("Shutdown task built. Registered shutdown task.");
        }

        double timeToStartInMillis = (double) (System.nanoTime() - startTime) / 1000000000;
        timeToStartInMillis *= 100;
        timeToStartInMillis = Math.round(timeToStartInMillis);
        timeToStartInMillis /= 100;
        Logger.info("Started in " + timeToStartInMillis + "s!");


    }

    @Override
    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public String getName() {
        return ProjectVariables.NAME;
    }

    @Override
    public String getVersion() {
        return ProjectVariables.VERSION;
    }

    @Override
    public String getMinestomVersion() {
        return ProjectVariables.MINESTOM_VERSION;
    }

    @Override
    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public World getDefaulWorld() {
        return worldManager.createWorld(WorldManagerImpl.DEFAULT_WORLD_NAME, Dimension.OVERWORLD);
    }

    private void setupDefaultWorld() {
        InstanceContainer instanceContainer = getDefaulWorld().getInstanceContainer();
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (DEBUG) {
                Logger.info("Player " + event.getPlayer().getUsername() + " is spawning in default world.");
            }
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(0, 41, 0));
            if (DEBUG) {
                Logger.info("Player " + event.getPlayer().getUsername() + " spawned in default world.");
            }
        });
    }

    private Runnable buildShutdownTask() {
        return () -> {
            long startTime = System.nanoTime();
            Logger.info("Saving worlds. This may take a while..");
            worldManager.saveAllWorlds();
            Logger.info("Saved all worlds in " + String.format("%.2f", (System.nanoTime() - startTime) / 1_000_000_000.0) + "s");
            Logger.info("Shutting down..");
        };
    }

    private void setViewDistance(String key, int value) {
        if (System.getProperty(key) != null) {
            return;
        }
        System.setProperty(key, Integer.valueOf(value).toString());
    }
}
