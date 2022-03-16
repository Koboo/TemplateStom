package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandFly extends Command {

    public CommandFly() {
        super("fly");

        setCondition(Conditions::playerOnly);

        addSyntax(this::executeSelf);
        addSyntax(this::executeOther, Arguments.TARGET);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_FLY)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        player.setAllowFlying(!player.isAllowFlying());
        player.sendMessage("Fly is now " + (player.isAllowFlying() ? "enabled" : "disabled"));
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_FLY_OTHER)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if (target == null) {
            player.sendMessage("The target is unavailable.");
            return;
        }

        target.setAllowFlying(!target.isAllowFlying());
        String mode = player.isAllowFlying() ? "enabled" : "disabled";
        target.sendMessage("Fly is now " + mode);

        player.sendMessage(target.getUsername() + "'s fly is now " + mode);
    }
}
