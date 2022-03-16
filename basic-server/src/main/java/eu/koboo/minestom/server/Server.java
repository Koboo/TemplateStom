package eu.koboo.minestom.server;

import eu.koboo.minestom.config.ProxyConfig;
import eu.koboo.minestom.config.QueryConfig;
import eu.koboo.minestom.config.ServerConfig;
import eu.koboo.minestom.console.Console;
import eu.koboo.minestom.server.chunk.FlatGenerator;
import eu.koboo.minestom.server.commands.CommandFly;
import eu.koboo.minestom.server.commands.CommandFlySpeed;
import eu.koboo.minestom.server.commands.CommandGameMode;
import eu.koboo.minestom.server.commands.CommandSpawn;
import eu.koboo.minestom.server.commands.CommandSpectate;
import eu.koboo.minestom.server.commands.CommandStop;
import eu.koboo.minestom.server.commands.CommandTeleport;
import eu.koboo.minestom.server.commands.CommandTeleportHere;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.extras.query.Query;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import org.tinylog.Logger;

public class Server {

    @Getter
    private static Server instance;

    @Getter
    private final ServerConfig serverConfig;
    @Getter
    private final ProxyConfig proxyConfig;
    @Getter
    private final QueryConfig queryConfig;

    @Getter
    private final Console console;

    public Server() {
        instance = this;

        Logger.info("Loading Server settings..");
        serverConfig = ServerConfig.load();

        Logger.info("Loading Proxy settings..");
        proxyConfig = ProxyConfig.load();

        Logger.info("Loading Query settings..");
        queryConfig = QueryConfig.load();

        Logger.info("Initializing console..");
        console = new Console();

        // TODO: Config values
        // open-to-lan
        //   otl-config

        Logger.info("Initializing server..");
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setTerminalEnabled(false);

        MinecraftServer.getExceptionManager().setExceptionHandler(exc -> Logger.error("An unexpected error occurred! ", exc));

        Logger.info("Registering Commands..");
        MinecraftServer.getCommandManager().register(new CommandFly());
        MinecraftServer.getCommandManager().register(new CommandFlySpeed());
        MinecraftServer.getCommandManager().register(new CommandGameMode());
        MinecraftServer.getCommandManager().register(new CommandSpawn());
        MinecraftServer.getCommandManager().register(new CommandSpectate());
        MinecraftServer.getCommandManager().register(new CommandStop());
        MinecraftServer.getCommandManager().register(new CommandTeleport());
        MinecraftServer.getCommandManager().register(new CommandTeleportHere());

        Logger.info("Setting up Instances..");
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setChunkGenerator(new FlatGenerator());

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(0, 44, 0));
        });

        String host = serverConfig.host();
        int port = serverConfig.port();

        if(queryConfig.enable()) {
            int queryPort = queryConfig.port();
            if(queryPort == port) {
                Logger.error("Server and Query port are equal (" + port + "=" + queryPort + ")! Abort!");
                System.exit(0);
                return;
            }
            Query.start(queryPort);
            Logger.info("Started query @ 0.0.0.0:" + queryPort);
        }

        if(serverConfig.optifineSupport()) {
            OptifineSupport.enable();
            Logger.info("Enabled OptifineSupport!");
        }

        switch (proxyConfig.proxyMode()) {
            case NONE -> {
                MojangAuth.init();
                Logger.info("ProxyMode 'NONE', enabled MojangAuth.");
            }
            case VELOCITY -> {
                if(proxyConfig.proxySecret() == null || proxyConfig.proxySecret().equalsIgnoreCase("")) {
                    Logger.warn("ProxyMode 'VELOCITY' selected, but no proxy-secret set! Abort!");
                    System.exit(0);
                    break;
                }
                VelocityProxy.enable(proxyConfig.proxySecret());
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
    }

}
