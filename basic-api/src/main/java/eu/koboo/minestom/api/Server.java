package eu.koboo.minestom.api;

import lombok.Getter;
import net.minestom.server.entity.Player;

public abstract class Server {

    @Getter
    private static Server instance;

    public Server() {
        instance = this;
    }

    public abstract boolean isOperator(Player player);

    public abstract void setOperator(Player player, boolean value);

}
