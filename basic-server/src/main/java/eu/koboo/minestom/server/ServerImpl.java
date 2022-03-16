package eu.koboo.minestom.server;

import eu.koboo.minestom.api.Server;
import eu.koboo.minestom.config.CommandConfig;
import eu.koboo.minestom.config.OperatorConfig;
import eu.koboo.minestom.config.ProxyConfig;
import eu.koboo.minestom.config.QueryConfig;
import eu.koboo.minestom.config.ServerConfig;
import eu.koboo.minestom.console.Console;
import eu.koboo.minestom.server.chunk.FlatGenerator;
import eu.koboo.minestom.server.commands.CommandDeop;
import eu.koboo.minestom.server.commands.CommandFly;
import eu.koboo.minestom.server.commands.CommandFlySpeed;
import eu.koboo.minestom.server.commands.CommandGameMode;
import eu.koboo.minestom.server.commands.CommandOp;
import eu.koboo.minestom.server.commands.CommandSpawn;
import eu.koboo.minestom.server.commands.CommandSpectate;
import eu.koboo.minestom.server.commands.CommandStop;
import eu.koboo.minestom.server.commands.CommandSurface;
import eu.koboo.minestom.server.commands.CommandTeleport;
import eu.koboo.minestom.server.commands.CommandTeleportHere;
import java.util.Locale;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
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

public class ServerImpl extends Server {

    @Getter
    private static ServerImpl instance;

    @Getter
    private final Console console;

    private final OperatorConfig operatorConfig;

    public ServerImpl() {
        instance = this;

        Logger.info("Loading Server settings..");
        ServerConfig serverConfig = ServerConfig.load();

        Logger.info("Loading Proxy settings..");
        ProxyConfig proxyConfig = ProxyConfig.load();

        Logger.info("Loading Query settings..");
        QueryConfig queryConfig = QueryConfig.load();

        Logger.info("Loading Command file..");
        CommandConfig commandConfig = CommandConfig.load();

        Logger.info("Loading Operator file..");
        operatorConfig = OperatorConfig.load();

        Logger.info("Initializing console..");
        console = new Console();

        Logger.info("Initializing server..");
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setTerminalEnabled(false);

        MinecraftServer.getExceptionManager()
                .setExceptionHandler(exc -> Logger.error("An unexpected error occurred! ", exc));

        Logger.info("Registering Commands..");
        checkRegisterCommand(commandConfig, new CommandDeop());
        checkRegisterCommand(commandConfig, new CommandOp());
        checkRegisterCommand(commandConfig, new CommandFly());
        checkRegisterCommand(commandConfig, new CommandFlySpeed());
        checkRegisterCommand(commandConfig, new CommandGameMode());
        checkRegisterCommand(commandConfig, new CommandSpawn());
        checkRegisterCommand(commandConfig, new CommandSpectate());
        checkRegisterCommand(commandConfig, new CommandStop());
        checkRegisterCommand(commandConfig, new CommandSurface());
        checkRegisterCommand(commandConfig, new CommandTeleport());
        checkRegisterCommand(commandConfig, new CommandTeleportHere());

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

        if (queryConfig.enable()) {
            int queryPort = queryConfig.port();
            if (queryPort == port) {
                Logger.error("Server and Query port are equal (" + port + "=" + queryPort + ")! Abort!");
                System.exit(0);
                return;
            }
            Query.start(queryPort);
            Logger.info("Started query @ 0.0.0.0:" + queryPort);
        }

        if (serverConfig.optifineSupport()) {
            OptifineSupport.enable();
            Logger.info("Enabled OptifineSupport!");
        }

        switch (proxyConfig.proxyMode()) {
            case NONE -> {
                if (serverConfig.onlineMode()) {
                    MojangAuth.init();
                    Logger.info("ProxyMode 'NONE', enabled MojangAuth.");
                } else {
                    Logger.info("ProxyMode 'NONE', without MojangAuth.");
                }
            }
            case VELOCITY -> {
                if (proxyConfig.proxySecret() == null || proxyConfig.proxySecret().equalsIgnoreCase("")) {
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

    @Override
    public boolean isOperator(Player player) {
        return operatorConfig.getOperatorIdList().contains(player.getUuid());
    }

    @Override
    public void setOperator(Player player, boolean value) {
        if (value && !isOperator(player)) {
            operatorConfig.getOperatorIdList().add(player.getUuid());
            OperatorConfig.write(operatorConfig.getOperatorIdList());
        } else if (!value && isOperator(player)) {
            operatorConfig.getOperatorIdList().remove(player.getUuid());
            OperatorConfig.write(operatorConfig.getOperatorIdList());
        }
    }

    private void checkRegisterCommand(CommandConfig commandConfig, Command command) {
        String name = command.getName();
        if (name.equalsIgnoreCase("stop")) {
            MinecraftServer.getCommandManager().register(command);
            return;
        }
        if (!commandConfig.getCommandNames().contains(name.toLowerCase(Locale.ROOT))) {
            Logger.warn("Disabling command '" + name + "'");
            return;
        }
        MinecraftServer.getCommandManager().register(command);
    }

}
