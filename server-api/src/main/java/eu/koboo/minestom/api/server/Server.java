package eu.koboo.minestom.api.server;

import eu.koboo.minestom.api.config.ServerConfig;
import eu.koboo.minestom.api.world.World;
import eu.koboo.minestom.api.world.manager.WorldManager;
import lombok.Getter;
import lombok.NonNull;

/**
 * Abstract class to represent the actual implementation of the server
 */
public abstract class Server {

    @Getter
    @NonNull
    private static Server instance;

    public Server(String[] args) {
        instance = this;
    }

    /**
     * Get the current loaded ServerConfig
     *
     * @return The loaded ServerConfig object
     */
    @NonNull
    public abstract ServerConfig getServerConfig();

    /**
     * Get the name of the project
     *
     * @return The project/server name
     */
    @NonNull
    public abstract String getName();

    /**
     * Get the version of the project
     * Note: This is not the JDK, Gradle or Minecraft version.
     *
     * @return The project/server version
     */
    @NonNull
    public abstract String getVersion();

    /**
     * Get the version of the Minestom library
     *
     * @return The used Minestom version
     */
    @NonNull
    public abstract String getMinestomVersion();

    /**
     * Get the world manager instance
     *
     * @return The world manager instance
     */
    @NonNull
    public abstract WorldManager getWorldManager();

    /**
     * Get the default world
     *
     * @return The default world
     */
    @NonNull
    public abstract World getDefaulWorld();

}
