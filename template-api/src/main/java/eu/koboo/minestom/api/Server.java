package eu.koboo.minestom.api;

import lombok.Getter;

public abstract class Server {

    @Getter
    private static Server instance;

    public Server() {
        instance = this;
    }

}
