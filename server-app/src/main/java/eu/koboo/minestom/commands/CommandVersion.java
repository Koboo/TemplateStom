package eu.koboo.minestom.commands;

import eu.koboo.minestom.api.server.Server;
import eu.koboo.minestom.server.ServerImpl;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class CommandVersion extends Command {

    public CommandVersion() {
        super("version");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Server: " + Server.getInstance().getName()));
            sender.sendMessage(Component.text("Version: " + Server.getInstance().getVersion()));
            sender.sendMessage(Component.text("Minestom: " + Server.getInstance().getMinestomVersion()));
        });
    }

}