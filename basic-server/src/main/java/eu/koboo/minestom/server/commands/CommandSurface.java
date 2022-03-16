package eu.koboo.minestom.server.commands;

import eu.koboo.minestom.server.utilities.Arguments;
import eu.koboo.minestom.server.utilities.Permissions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.ClientPacketsHandler.Play;
import org.jetbrains.annotations.NotNull;

public class CommandSurface extends Command {

    public CommandSurface() {
        super("surface");

        setCondition(Conditions::playerOnly);

        addSyntax(this::executeSelf);
        addSyntax(this::executeOther, Arguments.TARGET);
    }

    private void executeSelf(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (!player.hasPermission(Permissions.COMMAND_SURFACE)) {
            player.sendMessage(Permissions.MESSAGE_NO_PERMISSION);
            return;
        }

        toSurface(player);
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

        toSurface(target);
        player.sendMessage("Teleported " + target.getUsername() + " to surface");
    }

    private void toSurface(Player player) {
        Pos pos = player.getPosition();
        Instance instanceContainer = player.getInstance();
        if(instanceContainer == null) {
            player.sendMessage("No instance available.");
            return;
        }
        boolean previousBlockAir = false;
        Pos surface = null;
        for(int i = 0; i < 800; i++) {
            Block block = instanceContainer.getBlock(pos.blockX(), pos.blockY() + i, pos.blockZ());
            if(block.isAir()) {
                if(previousBlockAir) {
                    surface = new Pos(pos.blockX(), pos.blockY() + i, pos.blockZ());
                    break;
                }
                previousBlockAir = true;
            }
        }
        if(surface == null) {
            player.sendMessage("Surface not found.");
            return;
        }

        player.teleport(surface);
        player.sendMessage("Teleported to surface");
    }
}
