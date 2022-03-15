package eu.koboo.minestom.server.utilities;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;

public class CommandConditions {

    public static boolean playerOnly(CommandSender sender, String command) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("The command is only available for players."));
            return false;
        }
        return true;
    }


    public static boolean playerPermissions(CommandSender sender, String command, String permission) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("The command is only available for players."));
            return false;
        }
        return true;
    }

    public static boolean consoleOnly(CommandSender sender, String command) {
        if (!(sender instanceof ConsoleSender)) {
            sender.sendMessage(Component.text("The command is only available for the console."));
            return false;
        }
        return true;
    }

}
