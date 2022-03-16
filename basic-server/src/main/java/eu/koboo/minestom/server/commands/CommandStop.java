package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class CommandStop extends Command {

    public CommandStop() {
        super("stop", "end", "shutdown");
        setCondition((sender, command) -> {
            if(sender instanceof Player && !sender.hasPermission(Permissions.COMMAND_STOP)) {
                sender.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
                return false;
            }
            return true;
        });
        setDefaultExecutor((sender, context) -> MinecraftServer.stopCleanly());
    }

}
