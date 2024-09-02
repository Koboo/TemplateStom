package eu.koboo.minestom.server.world;

import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import eu.koboo.minestom.api.world.manager.WorldManager;

public class WorldManagerImpl implements WorldManager {
    @Override
    public World createWorld(String name, Dimension dimensionType) {
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
