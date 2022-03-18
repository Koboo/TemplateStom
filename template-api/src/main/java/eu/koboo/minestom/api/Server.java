package eu.koboo.minestom.api;

import eu.koboo.minestom.api.config.ServerConfig;
import lombok.Getter;

public abstract class Server {

    @Getter
    private static Server instance;

    public Server() {
        instance = this;
    }

    public abstract ServerConfig getServerConfig();

}
