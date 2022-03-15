package eu.koboo.minestom.server;

import eu.koboo.minestom.server.chunk.FlatGenerator;
import eu.koboo.minestom.server.commands.StopCommand;
import eu.koboo.minestom.terminal.Console;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import org.tinylog.Logger;

public class Server {

    private static Server instance;

    public Server() {
        instance = this;

        Logger.info("Initializing console..");
        Console console = new Console();

        // TODO: Config values
        // host
        // mc-port
        // query-enable
        //   port
        // proxy-mode (none, bungee, velocity)
        //   velocity-secret
        // optifine-support
        // open-to-lan
        //   otl-config

        //MojangAuth.init();
        //VelocityProxy.enable("");
        //BungeeCordProxy.enable();
        //OptifineSupport.enable();
        //Query.start(port)
        //OpenToLAN.open()

        Logger.info("Initializing server..");
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setTerminalEnabled(false);

        MinecraftServer.getExceptionManager().setExceptionHandler(exc -> Logger.error("An unexpected error occurred! ", exc));

        Logger.info("Registering Commands..");
        MinecraftServer.getCommandManager().register(new StopCommand());

        Logger.info("Setting up Instances..");
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setChunkGenerator(new FlatGenerator());

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
            event.getPlayer().setRespawnPoint(new Pos(0, 44, 0));
        });

        // TODO: Config
        String host = "0.0.0.0";
        int port = 25565;
        Logger.info("Starting @ " + host + ":" + port);

        minecraftServer.start(host, port);
        Logger.info("Listening on " + host + ":" + port);

        Logger.info("Starting console..");
        console.start();
    }

}
