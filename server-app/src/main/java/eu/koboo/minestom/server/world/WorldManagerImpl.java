package eu.koboo.minestom.server.world;

import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import eu.koboo.minestom.api.world.manager.WorldManager;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class WorldManagerImpl implements WorldManager {

    Map<String, World> loadedWorlds;
    Map<String, InstanceContainer> loadedInstances;

    public static final String DEFAULT_WORLD_NAME = "world";

    public WorldManagerImpl() {
        this.loadedWorlds = new HashMap<>();
        this.loadedInstances = new HashMap<>();
    }

    @Override
    public World createWorld(String name, Dimension dimensionType) {
        long startTime = System.nanoTime();
        World createdWorld = new World();
        Path dir = Path.of("worlds/" + name);
        if (Files.exists(dir)) {
            Logger.warn("World directory already exists; loading instead");
            InstanceContainer createdInstance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/" + name));
            createdWorld.setName(name);
            createdWorld.setInstanceContainer(createdInstance);
            createdWorld.setDimensionType(dimensionType.getDimensionType());
            loadedWorlds.put(name, createdWorld);
            loadedInstances.put(name, createdInstance);
            Logger.info("World loaded: " + name);
            return createdWorld;
        }
        try {
            Files.createDirectory(dir);
            Path defaultRegionFolder = getDefaultWorldRegionFolder();
            Files.walk(defaultRegionFolder)
                    .filter(Files::isRegularFile)
                    .forEach(source -> {
                        try {
                            Path destination = dir.resolve(defaultRegionFolder.relativize(source));
                            Files.createDirectories(destination.getParent());
                            Files.copy(source, destination);
                        } catch (IOException e) {
                            Logger.error("Failed to copy default world region files", e);
                        }
                    });
            InstanceContainer createdInstance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/" + name));
            createdWorld.setName(name);
            createdWorld.setInstanceContainer(createdInstance);
            createdWorld.setDimensionType(dimensionType.getDimensionType());
            loadedWorlds.put(name, createdWorld);
            loadedInstances.put(name, createdInstance);
            double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
            Logger.info("World created in " + String.format("%.2fms", timeInMillis) + ": " + name);
            return createdWorld;
        } catch (IOException e) {
            Logger.error("Failed to create world directory", e);
            return null;
        }
    }

    @Override
    public void deleteWorld(World world) {
        long startTime = System.nanoTime();
        String name = world.getName();
        World deletedWorld = loadedWorlds.get(name);
        if (deletedWorld == null) {
            return;
        }
        Path dir = Path.of("worlds/" + name);
        if (Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.walk(dir)
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                Logger.error("Failed to delete world directory", e);
                return;
            }
        }
        loadedWorlds.remove(name);
        loadedInstances.remove(name);
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        instanceManager.unregisterInstance(deletedWorld.getInstanceContainer());
        double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
        Logger.info("World deleted in " + String.format("%.2fms", timeInMillis) + ": " + name);
    }

    @Override
    public World getWorld(String name) {
        return loadedWorlds.get(name);
    }

    @Override
    public World[] getWorlds() {
        return loadedWorlds.values().toArray(new World[0]);
    }

    @Override
    public void unloadWorld(World world) {
        long startTime = System.nanoTime();
        String name = world.getName();
        World unloadedWorld = loadedWorlds.get(name);
        if (unloadedWorld == null) {
            return;
        }
        Logger.info("Unloading world: " + name);
        InstanceContainer instance = unloadedWorld.getInstanceContainer();
        if (instance != null) {
            InstanceManager instanceManager = MinecraftServer.getInstanceManager();
            instanceManager.unregisterInstance(instance);
        }
        loadedWorlds.remove(name);
        loadedInstances.remove(name);
        double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
        Logger.info("World unloaded in " + String.format("%.2fms", timeInMillis) + ": " + name);
    }

    @Override
    public void loadWorld(String name) {
        long startTime = System.nanoTime();
        World world = loadedWorlds.get(name);
        if (world != null) {
            Logger.warn("World already loaded; skipping load");
            return;
        }
        Logger.info("Loading world: " + name);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/" + name));
        Path dir = Path.of("worlds/" + name);
        if (Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
            World loadedWorld = new World();
            loadedWorld.setName(name);
            loadedWorld.setInstanceContainer(instance);
            loadedWorld.setDimensionType(Dimension.OVERWORLD.getDimensionType());
            loadedWorlds.put(name, loadedWorld);
            loadedInstances.put(name, instance);
            double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
            Logger.info("World loaded in " + String.format("%.2fms", timeInMillis) + ": " + name);
            return;
        }
        Logger.error("World directory not found. To create a new world, use the createWorld method.");
    }

    @Override
    public void saveWorld(String name) {
        long startTime = System.nanoTime();
        World world = loadedWorlds.get(name);
        if (world == null) {
            Logger.warn("World not loaded; skipping save");
            return;
        }
        InstanceContainer instance = world.getInstanceContainer();
        if (instance != null) {
            instance.saveInstance().thenAccept(success -> {
                double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
                Logger.info("World saved in " + String.format("%.2fms", timeInMillis) + ": " + name);
            });
            return;
        }
        Logger.error("Instance not found; skipping save");
    }

    @Override
    public void saveAllWorlds() {
        Logger.info("Saving all worlds. This may take a while..");
        for (World world : loadedWorlds.values()) {
            saveWorld(world.getName());
        }
    }

    private Path getDefaultWorldRegionFolder() {
        URL resourceUrl = getClass().getClassLoader().getResource("worlds/default/region");
        if (resourceUrl == null) {
            throw new IllegalStateException("Default world region folder not found!");
        }
        try {
            return Paths.get(resourceUrl.toURI());
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert URL to Path", e);
        }
    }
}
