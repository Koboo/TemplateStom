package eu.koboo.minestom.commands;

import eu.koboo.minestom.api.server.Server;
import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandWorld extends Command {

    public CommandWorld() {
        super("world", "w", "worlds");
        setCondition((sender, command) -> sender.hasPermission("command.world"));
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Current loaded worlds (" + Server.getInstance().getWorldManager().getWorlds().length + "):");
            for (World world : Server.getInstance().getWorldManager().getWorlds()) {
                sender.sendMessage(" - " + world.getName());
            }
            sender.sendMessage("Use /world create <name> <dimension> to create a new world.");
            sender.sendMessage("Use /world delete <name> to delete a world.");
            sender.sendMessage("Use /world load <name> to load a world.");
            sender.sendMessage("Use /world unload <name> to unload a world.");
            sender.sendMessage("Use /world save <name> to save a world.");
            sender.sendMessage("Use /world save-all to save all worlds.");
            sender.sendMessage("Use /world go <name> to teleport to a world.");
            sender.sendMessage("Use /world config <name> to view the world configuration.");
            sender.sendMessage("Use /world config <name> <key> <value> to set the world configuration.");
        });

        addSubcommand(new CommandWorldCreate());
        addSubcommand(new CommandWorldDelete());
        addSubcommand(new CommandWorldLoad());
        addSubcommand(new CommandWorldUnload());
        addSubcommand(new CommandWorldSave());
        addSubcommand(new CommandWorldSaveAll());
        addSubcommand(new CommandWorldGo());
        addSubcommand(new CommandWorldConfig());
        addSubcommand(new CommandWorldConfigChange());

    }

    private static class CommandWorldCreate extends Command {

        public CommandWorldCreate() {
            super("create", "c");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world create <name> <dimension>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                String dimension = context.get("dimension");
                if (Server.getInstance().getWorldManager().getWorld(name) != null) {
                    sender.sendMessage("World with name " + name + " already exists.");
                    return;
                }
                Dimension dimensionType = Dimension.valueOf(dimension.toUpperCase());
                World world = Server.getInstance().getWorldManager().createWorld(name, dimensionType);
                sender.sendMessage("World " + world.getName() + " created.");
            }, ArgumentType.String("name"), ArgumentType.String("dimension"));
        }
    }

    private static class CommandWorldDelete extends Command {

        public CommandWorldDelete() {
            super("delete", "d");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world delete <name>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                World world = Server.getInstance().getWorldManager().getWorld(name);
                if (world == null) {
                    sender.sendMessage("World with name " + name + " does not exist.");
                    return;
                }
                Server.getInstance().getWorldManager().deleteWorld(world);
                sender.sendMessage("World " + world.getName() + " deleted.");
            }, ArgumentType.String("name"));
        }
    }

    private static class CommandWorldLoad extends Command {

        public CommandWorldLoad() {
            super("load", "l");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world load <name>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                Server.getInstance().getWorldManager().loadWorld(name);
                sender.sendMessage("World " + name + " loaded.");
            }, ArgumentType.String("name"));
        }
    }

    private static class CommandWorldUnload extends Command {

        public CommandWorldUnload() {
            super("unload", "u");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world unload <name>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                World world = Server.getInstance().getWorldManager().getWorld(name);
                if (world == null) {
                    sender.sendMessage("World with name " + name + " does not exist.");
                    return;
                }
                Server.getInstance().getWorldManager().unloadWorld(world);
                sender.sendMessage("World " + world.getName() + " unloaded.");
            }, ArgumentType.String("name"));
        }
    }

    private static class CommandWorldSave extends Command {

        public CommandWorldSave() {
            super("save", "s");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world save <name>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                Server.getInstance().getWorldManager().saveWorld(name);
                sender.sendMessage("World " + name + " saved.");
            }, ArgumentType.String("name"));
        }
    }

    private static class CommandWorldSaveAll extends Command {

        public CommandWorldSaveAll() {
            super("save-all", "sa");
            setDefaultExecutor((sender, context) -> {
                Server.getInstance().getWorldManager().saveAllWorlds();
                sender.sendMessage("All worlds saved.");
            });
        }
    }

    private static class CommandWorldGo extends Command {

        public CommandWorldGo() {
            super("go", "g");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world go <name>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                World world = Server.getInstance().getWorldManager().getWorld(name);
                if (world == null) {
                    sender.sendMessage("World with name " + name + " does not exist.");
                    return;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return;
                }
                ((Player) sender).teleport(world.getSpawnPoint());
                sender.sendMessage("Teleported to world " + world.getName() + "'s spawn.");
            }, ArgumentType.String("name"));
        }
    }

    private static class CommandWorldConfig extends Command {

        public CommandWorldConfig() {
            super("config", "cfg");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world config <name>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                World world = Server.getInstance().getWorldManager().getWorld(name);
                if (world == null) {
                    sender.sendMessage("World with name " + name + " does not exist.");
                    return;
                }
                sender.sendMessage("World " + world.getName() + " configuration:");
                for (String key : world.getWorldConfig().getKeys(false)) {
                    sender.sendMessage(" - " + key + ": " + world.getWorldConfig().get(key));
                }
            }, ArgumentType.String("name"));
        }
    }

    private static class CommandWorldConfigChange extends Command {

        public CommandWorldConfigChange() {
            super("config", "cfg");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Usage: /world config <name> <key> <value>");
            });
            addSyntax((sender, context) -> {
                String name = context.get("name");
                String key = context.get("key");
                String value = context.get("value");
                World world = Server.getInstance().getWorldManager().getWorld(name);
                if (world == null) {
                    sender.sendMessage("World with name " + name + " does not exist.");
                    return;
                }
                world.getWorldConfig().set(key, value);
                sender.sendMessage("World " + world.getName() + " configuration updated.");
            }, ArgumentType.String("name"), ArgumentType.String("key"), ArgumentType.String("value"));
        }
    }

}
