package eu.koboo.minestom.commands;

import eu.koboo.minestom.server.ServerImpl;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandStop extends Command {

    public CommandStop(ServerImpl server) {
        super("stop");
        setCondition((sender, command) ->
            !(sender instanceof Player) || sender.hasPermission("command.stop"));
        setDefaultExecutor((sender, context) -> server.stop());
    }

}