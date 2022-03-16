package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandSpawn extends Command {

    public CommandSpawn() {
        super("spawn");

        setCondition(Conditions::playerOnly);

        addSyntax(this::executeSelf);
        addSyntax(this::executeOther, Arguments.TARGET);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_SPAWN)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        player.teleport(player.getRespawnPoint());
        sender.sendMessage("Teleported to spawn");
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_SPAWN_OTHER)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("target");
        if(target == null || player.getInstance() == null) {
            player.sendMessage("The target is unavailable!");
            return;
        }

        target.teleport(target.getRespawnPoint());
        target.sendMessage("Teleported to spawn");

        player.sendMessage("Teleported " + target.getUsername() + " to spawn");
    }
}
