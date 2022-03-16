package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.Server;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import org.tinylog.Logger;

public class CommandReloadConfig extends Command {

    public CommandReloadConfig() {
        super("reloadconfig");
        setCondition(Conditions::consoleOnly);
        setDefaultExecutor((sender, context) -> {
            try {
                Server.getInstance().reloadConfig();
            } catch (Exception e) {
                Logger.error("Error while reloading config", e);
                return;
            }
            sender.sendMessage("Successful reloaded config.");
        });
    }

}
