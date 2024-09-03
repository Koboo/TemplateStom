package eu.koboo.minestom.api.world.manager;

import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.dimension.Dimension;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.world.DimensionType;
import org.simpleyaml.configuration.file.YamlFile;

public interface WorldManager {

    World createWorld(String name, Dimension dimensionType);

    void deleteWorld(World world);

    World getWorld(String name);

    World[] getWorlds();

    void unloadWorld(World world);

    void loadWorld(String name);

    void saveWorld(String name);

    void saveAllWorlds();

    YamlFile getWorldConfigFile(String worldName);

}
