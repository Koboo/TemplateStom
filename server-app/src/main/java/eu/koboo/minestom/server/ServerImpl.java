package eu.koboo.minestom.server;

import eu.koboo.minestom.api.config.ServerConfig;
import eu.koboo.minestom.api.server.Server;
import eu.koboo.minestom.commands.CommandStop;
import eu.koboo.minestom.commands.CommandVersion;
import eu.koboo.minestom.console.JLineConsole;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.Difficulty;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

@Slf4j
public class ServerImpl extends Server {

    @Getter
    private static ServerImpl instance;

    private final ServerConfig serverConfig;

    @Getter
    private final JLineConsole console;

    public ServerImpl() {
        long startTime = System.nanoTime();

        instance = this;

        log.info("Loading settings..");
        File configFile = new File(ServerConfig.FILE_NAME);
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(configFile)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();
            if (!configFile.exists()) {
                CommentedConfigurationNode node = loader.createNode(ConfigurationOptions.defaults());
                node.set(ServerConfig.class, new ServerConfig());
                loader.save(node);
            }
            CommentedConfigurationNode node = loader.load();
            serverConfig = node.get(ServerConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while exporting default configuration", e);
        }

        log.info("Initializing console..");
        console = new JLineConsole();

        log.info("Initializing server..");
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getExceptionManager()
                .setExceptionHandler(exc -> log.error("An unexpected error occurred! ", exc));

        log.info("Registering commands..");
        MinecraftServer.getCommandManager().register(new CommandStop(this));
        MinecraftServer.getCommandManager().register(new CommandVersion());

        log.info("Creating instance..");
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 36, Block.STONE);
            unit.modifier().fillHeight(37, 39, Block.DIRT);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    unit.modifier().setBlock(x, 40, z, Block.GRASS_BLOCK);
                }
            }
        });

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(0, 41, 0));
        });

        String host = serverConfig.getHost();
        int port = serverConfig.getPort();

        MinecraftServer.setBrandName(this.getName());
        MinecraftServer.setDifficulty(Difficulty.valueOf(serverConfig.getDifficulty()));

        MinecraftServer.setCompressionThreshold(serverConfig.getCompressionThreshold());

        setViewDistance("minestom.chunk-view-distance", serverConfig.getChunkViewDistance());
        setViewDistance("minestom.entity-view-distance", serverConfig.getEntityViewDistance());

        switch (serverConfig.getProxyMode()) {
            case NONE -> {
                if (serverConfig.isOnlineMode()) {
                    MojangAuth.init();
                    log.info("ProxyMode 'NONE', enabled MojangAuth.");
                } else {
                    log.info("ProxyMode 'NONE', without MojangAuth.");
                }
            }
            case VELOCITY -> {
                String velocitySecret = serverConfig.getVelocitySecret();
                if (velocitySecret == null || velocitySecret.equalsIgnoreCase("")) {
                    log.warn("ProxyMode 'VELOCITY' selected, but no proxy-secret set! Abort!");
                    System.exit(0);
                    break;
                }
                VelocityProxy.enable(velocitySecret);
                log.info("ProxyMode 'VELOCITY', enabled VelocityProxy.");
            }
            case BUNGEECORD -> {
                BungeeCordProxy.enable();
                log.info("ProxyMode 'BUNGEECORD', enabled BungeeCordProxy.");
            }
        }
        log.info("Starting @ {}:{}", host, port);

        minecraftServer.start(host, port);
        log.info("Listening on {}:{}", host, port);

        console.start();

        double timeToStartInSeconds = (double) (System.nanoTime() - startTime) / 1000000000;
        timeToStartInSeconds *= 100;
        timeToStartInSeconds = Math.round(timeToStartInSeconds);
        timeToStartInSeconds /= 100;
        log.info("Started in {}s!", timeToStartInSeconds);
    }

    public void stop() {
        log.info("Stopping..");
        MinecraftServer.stopCleanly();
        log.info("Goodbye!");
    }

    @Override
    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public String getName() {
        return CurrentBuild.NAME;
    }

    @Override
    public String getVersion() {
        return CurrentBuild.VERSION;
    }

    @Override
    public String getMinestomVersion() {
        return CurrentBuild.MINESTOM_VERSION;
    }

    private void setViewDistance(String key, int value) {
        if (System.getProperty(key) != null) {
            return;
        }
        System.setProperty(key, Integer.valueOf(value).toString());
    }
}
