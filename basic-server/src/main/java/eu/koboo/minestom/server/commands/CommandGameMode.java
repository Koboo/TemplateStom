package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacketsHandler.Play;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

public class CommandGameMode extends Command {

    @SuppressWarnings("all")
    public CommandGameMode() {
        super("gamemode", "gm");

        setCondition(Conditions::playerOnly);

        ArgumentEnum<GameMode> gameMode = ArgumentType.Enum("gameMode", GameMode.class)
                .setFormat(ArgumentEnum.Format.LOWER_CASED);

        Argument<GameMode> gameModeId = ArgumentType.Integer("gameModeId").between(0, 3).map(id -> GameMode.values()[id]);

        addSyntax((sender, context) -> executeSelf(sender, context.get("gameMode")), gameMode);
        addSyntax((sender, context) -> executeSelf(sender, context.get("gameModeId")), gameModeId);
        addSyntax((sender, context) -> executeOther(sender, context.get("target"), context.get("gameMode")), gameMode, Arguments.TARGET);
        addSyntax((sender, context) -> executeOther(sender, context.get("target"), context.get("gameModeId")), gameModeId, Arguments.TARGET);
    }

    private void executeSelf(@NotNull CommandSender sender, GameMode gamemode) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_GAMEMODE)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        if(gamemode == null) {
            player.sendMessage("Syntax: /gamemode <creative | survival | adventure | spectator>");
            return;
        }

        player.setGameMode(gamemode);
        sender.sendMessage("Set own game mode to " + gamemode.name().toLowerCase() + " Mode");
    }

    private void executeOther(@NotNull CommandSender sender, Player target, GameMode gamemode) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_GAMEMODE_OTHER)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        if(gamemode == null) {
            player.sendMessage("Syntax: /gamemode <creative | survival | adventure | spectator>");
            return;
        }

        if (target == null) {
            player.sendMessage("The target is unavailable.");
            return;
        }

        target.setGameMode(gamemode);
        target.sendMessage("Set own game mode to " + gamemode.name().toLowerCase() + " Mode");

        player.sendMessage(
                "Set " + target.getUsername() + "'s game mode to " + gamemode.name().toLowerCase() + " Mode");
    }
}
