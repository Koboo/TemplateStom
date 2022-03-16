package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandTeleport extends Command {

    public CommandTeleport() {
        super("teleport", "tp");

        setCondition(Conditions::playerOnly);

        ArgumentEntity tpTo = ArgumentType.Entity("to").onlyPlayers(true).singleEntity(true);
        ArgumentEntity tpTarget = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);

        addSyntax(this::executeSelf, tpTo);
        addSyntax(this::executeOther, tpTarget, tpTo);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_TELEPORT)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        Player target = context.get("to");
        if(target == null || target.getInstance() == null) {
            player.sendMessage("The target is unavailable!");
            return;
        }

        player.setInstance(target.getInstance());
        player.teleport(target.getPosition());
        sender.sendMessage("Teleported to " + target.getUsername());
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = context.get("to");
        if (!player.hasPermission(Permissions.COMMAND_TELEPORT_OTHER)) {
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

        sender.sendMessage("Teleported " + target.getUsername() + " to " + player.getUsername());
    }
}
