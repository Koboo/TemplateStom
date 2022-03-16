package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block.Getter.Condition;
import org.jetbrains.annotations.NotNull;

public class CommandTeleportHere extends Command {

    public CommandTeleportHere() {
        super("teleporthere", "tphere");

        setCondition(Conditions::playerOnly);

        addSyntax(this::executeSelf, Arguments.TARGET);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_TELEPORT_HERE)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if(target == null || player.getInstance() == null) {
            player.sendMessage("The target is unavailable!");
            return;
        }

        target.setInstance(player.getInstance());
        target.teleport(player.getPosition());
        sender.sendMessage("Teleported " + target.getUsername() + " to you");
    }

}
