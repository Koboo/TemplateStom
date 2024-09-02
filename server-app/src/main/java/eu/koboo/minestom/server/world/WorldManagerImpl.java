package eu.koboo.minestom.server.world;

import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import eu.koboo.minestom.api.world.manager.WorldManager;
import eu.koboo.minestom.files.PathWithFileSystem;
import eu.koboo.minestom.server.ServerImpl;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.simpleyaml.configuration.file.YamlFile;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
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
        try {
            if (!Files.exists(dir.getParent())) {
                Files.createDirectories(dir.getParent());
            } else {
                if (ServerImpl.DEBUG) Logger.info("Parent directory already exists: " + dir.getParent());
            }
            if (!Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectory(dir);
            } else {
                if (ServerImpl.DEBUG) Logger.info("World directory already exists: " + dir);
            }
            PathWithFileSystem defaultRegionFolderWithFS = getDefaultWorldRegionFolder();
            Path defaultRegionFolder = defaultRegionFolderWithFS.getPath();
            FileSystem fileSystem = defaultRegionFolderWithFS.getFileSystem();
            try {
                Files.walk(defaultRegionFolder)
                        .filter(Files::isRegularFile)
                        .forEach(source -> {
                            try {
                                Path destination = dir.resolve(defaultRegionFolder.relativize(source).toString());
                                Files.createDirectories(destination.getParent());
                                if (!Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
                                    Files.copy(source, destination);
                                } else {
                                    if (ServerImpl.DEBUG) Logger.info("File already exists; skipping copy: " + destination);
                                }
                            } catch (IOException e) {
                                Logger.error("Failed to copy default world region files", e);
                                e.printStackTrace();
                            }
                        });
            } finally {
                if (fileSystem != null) {
                    fileSystem.close();
                }
            }
            InstanceContainer createdInstance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/" + name));
            createdWorld.setName(name);
            createdWorld.setInstanceContainer(createdInstance);
            createdWorld.setDimensionType(dimensionType.getDimensionType());
            YamlFile yamlFile = createdWorld.getWorldConfig();
            if (yamlFile != null) {
                try {
                    yamlFile.save();
                    createdWorld.setWorldConfig(yamlFile);
                } catch (IOException e) {
                    Logger.error("Failed to save world configuration", e);
                }
            } else {
                YamlFile newConfig = getConfig(name);
                createdWorld.setWorldConfig(newConfig);
            }
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

    public void loadAllAvailableWorlds() {
        long startTime = System.nanoTime();
        File[] files = new File("worlds").listFiles();
        if (files == null) {
            return;
        }
        Arrays.stream(files)
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.equals(DEFAULT_WORLD_NAME))
                .filter(name -> !loadedWorlds.containsKey(name))
                .forEach(this::loadWorld);
        double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
        Logger.info("All available worlds loaded in " + String.format("%.2fms", timeInMillis));
    }

    @Override
    public void deleteWorld(World world) {
        long startTime = System.nanoTime();
        String name = world.getName();
        World deletedWorld = loadedWorlds.get(name);
        if (deletedWorld == null) {
            return;
        }
        YamlFile yamlFile = deletedWorld.getWorldConfig();
        if (yamlFile != null) {
            try {
                yamlFile.save();
            } catch (IOException e) {
                Logger.error("Failed to save world configuration", e);
            }
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
        YamlFile yamlFile = unloadedWorld.getWorldConfig();
        if (yamlFile != null) {
            try {
                yamlFile.save();
            } catch (IOException e) {
                Logger.error("Failed to save world configuration", e);
            }
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
            if (ServerImpl.DEBUG) Logger.warn("World already loaded; skipping load");
            return;
        }
        if (ServerImpl.DEBUG) Logger.info("Loading world: " + name);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/" + name));
        Path dir = Path.of("worlds/" + name);
        if (Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
            World loadedWorld = new World();
            loadedWorld.setName(name);
            loadedWorld.setInstanceContainer(instance);
            loadedWorld.setDimensionType(Dimension.OVERWORLD.getDimensionType());

            // Set spawn point
            Pos spawnPoint = loadConfig(name);

            loadedWorld.setSpawnPoint(spawnPoint);
            loadedWorld.setWorldConfig(getConfig(name));

            loadedWorlds.put(name, loadedWorld);
            loadedInstances.put(name, instance);
            double timeInMillis = (System.nanoTime() - startTime) / 1_000_000.0;
            if (ServerImpl.DEBUG) Logger.info("World loaded in " + String.format("%.2fms", timeInMillis) + ": " + name);
            return;
        }
        Logger.error("World directory not found. To create a new world, use the createWorld method.");
    }

    @Override
    public void saveWorld(String name) {
        long startTime = System.nanoTime();
        World world = loadedWorlds.get(name);
        YamlFile yamlFile = world.getWorldConfig();
        if (world == null) {
            Logger.warn("World not loaded; skipping save");
            return;
        }
        if (yamlFile != null) {
            try {
                yamlFile.save();
            } catch (IOException e) {
                Logger.error("Failed to save world configuration", e);
            }
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

    @Override
    public Pos getSpawnPoint(World world) {
        return world.getSpawnPoint();
    }

    private PathWithFileSystem getDefaultWorldRegionFolder() {
        URL resourceUrl = getClass().getClassLoader().getResource("worlds/default");
        if (resourceUrl == null) {
            throw new IllegalStateException("Default world region folder not found!");
        }
        try {
            URI uri = resourceUrl.toURI();
            if ("jar".equals(uri.getScheme())) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                return new PathWithFileSystem(fileSystem.getPath("worlds/default"), fileSystem);
            } else {
                return new PathWithFileSystem(Paths.get(uri), null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert URL to Path", e);
        }
    }

    private YamlFile getConfig(String worldName) {
        try {
            Path dir = Path.of("worlds/" + worldName);
            YamlFile yamlFile = new YamlFile(dir.resolve("minestom-world.yml").toString());
            yamlFile.options().copyDefaults(true);
            if (!yamlFile.exists()) {
                yamlFile.createNewFile();
            }
            yamlFile.loadWithComments();
            defaultValue(yamlFile, "spawn.x", 0.0D, "The x-coordinate of the spawnpoint");
            defaultValue(yamlFile, "spawn.y", 41.0D, "The y-coordinate of the spawnpoint");
            defaultValue(yamlFile, "spawn.z", 0.0D, "The z-coordinate of the spawnpoint");
            defaultValue(yamlFile, "spawn.yaw", 0.0F, "The yaw of the spawnpoint");
            defaultValue(yamlFile, "spawn.pitch", 0.0F, "The pitch of the spawnpoint");
            yamlFile.save();
            return yamlFile;
        } catch (IOException e) {
            Logger.error("Failed to load world configuration", e);
        }
        return null;
    }

    private Pos loadConfig(String worldName) {
        try {
            Path dir = Path.of("worlds/" + worldName);
            YamlFile yamlFile = new YamlFile(dir.resolve("minestom-world.yml").toString());
            yamlFile.options().copyDefaults(true);
            if (!yamlFile.exists()) {
                yamlFile.createNewFile();
            }
            yamlFile.loadWithComments();

            defaultValue(yamlFile, "spawn.x", 0.0D, "The x-coordinate of the spawnpoint");
            defaultValue(yamlFile, "spawn.y", 41.0D, "The y-coordinate of the spawnpoint");
            defaultValue(yamlFile, "spawn.z", 0.0D, "The z-coordinate of the spawnpoint");
            defaultValue(yamlFile, "spawn.yaw", 0.0F, "The yaw of the spawnpoint");
            defaultValue(yamlFile, "spawn.pitch", 0.0F, "The pitch of the spawnpoint");

            yamlFile.save();

            double x = yamlFile.getInt("spawn.x");
            double y = yamlFile.getInt("spawn.y");
            double z = yamlFile.getInt("spawn.z");
            float yaw = yamlFile.getInt("spawn.yaw");
            float pitch = yamlFile.getInt("spawn.pitch");

            return new Pos(x, y, z, yaw, pitch);
        } catch (IOException e) {
            Logger.error("Failed to load world configuration", e);
        }
        return new Pos(0.0D, 41.0D, 0.0D, 0.0F, 0.0F);
    }

    private void defaultValue(YamlFile yamlFile, String key, Object value, String comment) {
        if(!yamlFile.contains(key)) {
            yamlFile.set(key, value);
            yamlFile.setComment(key, comment);
        }
    }
}
