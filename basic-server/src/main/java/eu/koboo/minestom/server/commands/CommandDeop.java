package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.ServerImpl;
import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDeop extends Command {

    public CommandDeop() {
        super("deoperator", "deop");

        addSyntax(this::executeOther, Arguments.TARGET);
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.hasPermission(Permissions.COMMAND_OPERATOR) && !(sender instanceof ConsoleSender)) {
            sender.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if(target == null) {
            sender.sendMessage("The target is unavailable!");
            return;
        }

        if(!ServerImpl.getInstance().isOperator(target)) {
            sender.sendMessage(target.getUsername() + " isn't an operator.");
            return;
        }

        ServerImpl.getInstance().setOperator(target, false);
        sender.sendMessage(target.getUsername() + " is no longer an operator.");
    }
}
