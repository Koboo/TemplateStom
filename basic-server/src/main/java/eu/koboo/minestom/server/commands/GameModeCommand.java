package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacketsHandler.Play;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

public class GameModeCommand extends Command {

    public GameModeCommand() {
        super("gamemode", "gm");

        setCondition((sender, command) -> {
            if(sender instanceof ConsoleSender) {
                sender.sendMessage("Only for the players.");
                return false;
            }
            return true;
        });

        ArgumentEnum<GameMode> gameMode = ArgumentType.Enum("gameMode", GameMode.class)
                .setFormat(ArgumentEnum.Format.LOWER_CASED);

        ArgumentEntity other = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        addSyntax(this::executeSelf, gameMode);
        addSyntax(this::executeOther, gameMode, other);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        GameMode gamemode = context.get("gameMode");

        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_GAMEMODE)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        player.setGameMode(gamemode);
        sender.sendMessage("Set own game mode to " + gamemode.name().toLowerCase() + " Mode");
    }

    private void executeOther(@NotNull CommandSender sender, @NotNull CommandContext context) {
        GameMode gamemode = context.get("gamemode");
        Player player = (Player) sender;

        if (!player.hasPermission(Permissions.COMMAND_GAMEMODE_OTHER)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        EntityFinder targetFinder = context.get("player");
        Player target = targetFinder.findFirstPlayer(sender);

        if (target == null) {
            sender.sendMessage("The target is unavailable.");
            return;
        }

        target.setGameMode(gamemode);
        sender.sendMessage(
                "Set " + target.getUsername() + "'s game mode to " + gamemode.name().toLowerCase() + " Mode");
    }
}
