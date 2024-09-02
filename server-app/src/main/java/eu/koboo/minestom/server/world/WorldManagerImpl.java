package eu.koboo.minestom.server.world;

import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import eu.koboo.minestom.api.world.manager.WorldManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

public class WorldManagerImpl implements WorldManager {

    Map<String, World> loadedWorlds;
    Map<String, InstanceContainer> loadedInstances;

    @Override
    public World createWorld(String name, Dimension dimensionType) {
        World createdWorld = new World();
        Path dir = Path.of("worlds/" + name);
        if (Files.exists(dir)) {
            InstanceContainer createdInstance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/" + name));
            createdWorld.setName(name);
            createdWorld.setInstanceContainer(createdInstance);
            createdWorld.setDimensionType(dimensionType.getDimensionType());
            loadedWorlds.put(name, createdWorld);
            loadedInstances.put(name, createdInstance);
            return createdWorld;
        }
        try {
            Files.createDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteWorld(World world) {

    }

    @Override
    public World getWorld(String name) {
        return null;
    }

    @Override
    public World[] getWorlds() {
        return new World[0];
    }

    @Override
    public void unloadWorld(World world) {

    }

    @Override
    public void loadWorld(String name) {

    }

    @Override
    public void saveWorld(String name) {

    }

    @Override
    public void saveAllWorlds() {

    }
}
