package eu.koboo.minestom.server;

import eu.koboo.minestom.api.config.ServerConfig;
import eu.koboo.minestom.api.server.Server;
import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import eu.koboo.minestom.api.world.manager.WorldManager;
import eu.koboo.minestom.commands.CommandStop;
import eu.koboo.minestom.commands.CommandVersion;
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

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ServerImpl extends Server {

    @Getter
    @NonFinal
    static ServerImpl instance;

    ServerConfig serverConfig;

    WorldManagerImpl worldManager;

    @Getter
    Console console;

    public ServerImpl() {
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

        Logger.info("Creating instance..");
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = getDefaulWorld().getInstanceContainer();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(0, 41, 0));
        });

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
                    Logger.info("ProxyMode 'NONE', without MojangAuth.");
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

        minecraftServer.start(host, port);
        Logger.info("Listening on " + host + ":" + port);

        console.start();

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

    private void setViewDistance(String key, int value) {
        if (System.getProperty(key) != null) {
            return;
        }
        System.setProperty(key, Integer.valueOf(value).toString());
    }
}
