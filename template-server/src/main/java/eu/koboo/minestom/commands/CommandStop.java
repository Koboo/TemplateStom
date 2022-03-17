package eu.koboo.minestom.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class CommandStop extends Command {

    public CommandStop() {
        super("stop");
        setCondition((sender, command) ->
            !(sender instanceof Player) || sender.hasPermission("command.stop"));
        setDefaultExecutor((sender, context) -> MinecraftServer.stopCleanly());
    }

}