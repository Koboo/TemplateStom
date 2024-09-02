package eu.koboo.minestom.api.server;

import eu.koboo.minestom.api.config.ServerConfig;
import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.manager.WorldManager;
import lombok.Getter;

/**
 * Abstract class to represent the actual implementation of the server
 */
public abstract class Server {

    @Getter
    private static Server instance;

    public Server() {
        instance = this;
    }

    /**
     * Get the current loaded ServerConfig
     *
     * @return The loaded ServerConfig object
     */
    public abstract ServerConfig getServerConfig();

    /**
     * Get the name of the project
     *
     * @return The project/server name
     */
    public abstract String getName();

    /**
     * Get the version of the project
     * Note: This is not the JDK, Gradle or Minecraft version.
     *
     * @return The project/server version
     */
    public abstract String getVersion();

    /**
     * Get the version of the Minestom library
     *
     * @return The used Minestom version
     */
    public abstract String getMinestomVersion();

    public abstract WorldManager getWorldManager();

    public abstract World getDefaulWorld();

}
