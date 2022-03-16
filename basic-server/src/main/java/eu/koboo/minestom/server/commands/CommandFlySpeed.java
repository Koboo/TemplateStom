package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandFlySpeed extends Command {

    public CommandFlySpeed() {
        super("flyspeed");

        setCondition(Conditions::playerOnly);

        ArgumentNumber<Float> flySpeed = ArgumentType.Float("flySpeed");

        addSyntax(this::showSelf);
        addSyntax(this::showOther, Arguments.TARGET);
        addSyntax(this::executeSelf, flySpeed);
        addSyntax(this::executeOther, Arguments.TARGET, flySpeed);
    }

    private void showSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_FLY_SPEED)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        player.sendMessage("Current FlySpeed is " + player.getFlyingSpeed());
    }

    private void showOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_FLY_SPEED_OTHER)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if (target == null) {
            player.sendMessage("The target is unavailable.");
            return;
        }

        player.sendMessage(target.getUsername() + "'s FlySpeed is " + target.getFlyingSpeed());
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_FLY_SPEED)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Float flySpeed = context.get("flySpeed");
        if(flySpeed == null) {
            player.sendMessage("No flyspeed given!");
            return;
        }
        player.setFlyingSpeed(flySpeed);
        player.sendMessage("FlySpeed is now " + flySpeed);
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_FLY_SPEED_OTHER)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if (target == null) {
            player.sendMessage("The target is unavailable.");
            return;
        }

        Float flySpeed = context.get("flySpeed");
        if(flySpeed == null) {
            player.sendMessage("No flyspeed given!");
            return;
        }

        target.setFlyingSpeed(flySpeed);
        target.sendMessage("FlySpeed is now " + flySpeed);

        player.sendMessage(target.getUsername() + "'s FlySpeed is now " + flySpeed);
    }
}
