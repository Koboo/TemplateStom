package eu.koboo.minestom.server;

import eu.koboo.minestom.config.ServerConfig;
import eu.koboo.minestom.console.Console;
import eu.koboo.minestom.server.chunk.FlatGenerator;
import eu.koboo.minestom.server.commands.CommandFly;
import eu.koboo.minestom.server.commands.CommandGameMode;
import eu.koboo.minestom.server.commands.CommandReloadConfig;
import eu.koboo.minestom.server.commands.CommandSpawn;
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
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import org.tinylog.Logger;

public class Server {

    @Getter
    private static Server instance;

    @Getter
    private ServerConfig serverConfig;

    @Getter
    private final Console console;

    public Server() {
        instance = this;

        Logger.info("Loading config..");
        serverConfig = ServerConfig.load();

        Logger.info("Initializing console..");
        console = new Console();

        // TODO: Config values
        // query-enable
        //   port
        // optifine-support
        // open-to-lan
        //   otl-config

        Logger.info("Initializing server..");
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setTerminalEnabled(false);

        MinecraftServer.getExceptionManager().setExceptionHandler(exc -> Logger.error("An unexpected error occurred! ", exc));

        Logger.info("Registering Commands..");
        MinecraftServer.getCommandManager().register(new CommandFly());
        MinecraftServer.getCommandManager().register(new CommandGameMode());
        MinecraftServer.getCommandManager().register(new CommandReloadConfig());
        MinecraftServer.getCommandManager().register(new CommandSpawn());
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
        Logger.info("Starting @ " + host + ":" + port);

        switch (serverConfig.proxyMode()) {
            case NONE -> {
                MojangAuth.init();
                Logger.info("ProxyMode 'NONE', enabled MojangAuth.");
            }
            case VELOCITY -> {
                if(serverConfig.proxySecret() == null || serverConfig.proxySecret().equalsIgnoreCase("")) {
                    Logger.warn("ProxyMode 'VELOCITY' selected, but no proxy-secret set!");
                }
                VelocityProxy.enable(serverConfig.proxySecret());
                Logger.info("ProxyMode 'VELOCITY', enabled VelocityProxy.");
            }
            case BUNGEECORD -> {
                BungeeCordProxy.enable();
                Logger.info("ProxyMode 'BUNGEECORD', enabled BungeeCordProxy.");
            }
        }

        minecraftServer.start(host, port);
        Logger.info("Listening on " + host + ":" + port);

        Logger.info("Starting console..");
        console.start();
    }

    public void reloadConfig() {
        this.serverConfig = ServerConfig.load();
    }

}
