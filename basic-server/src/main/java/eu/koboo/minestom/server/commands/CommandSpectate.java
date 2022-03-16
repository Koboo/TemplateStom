package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandSpectate extends Command {

    public CommandSpectate() {
        super("spectate");

        setCondition(Conditions::playerOnly);

        addSyntax(this::executeSelf);
        addSyntax(this::executeOther, Arguments.TARGET);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_SPECTATE)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        player.stopSpectating();
        player.sendMessage("Stopped spectating.");
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_SPECTATE)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if (target == null) {
            player.sendMessage("The target is unavailable.");
            return;
        }

        player.spectate(target);
        player.sendMessage("You're now spectating " + target.getUsername() + ". Type /spectate to stop.");
    }
}
