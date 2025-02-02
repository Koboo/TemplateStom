package eu.koboo.minestom.api.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minestom.server.world.Difficulty;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * Config object to represent the `configuration.yml` file
 */
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class ServerConfig {

    public static final String FILE_NAME = "configuration.yml";

    @Setting("network.host")
    String host = "127.0.0.1";

    @Setting("network.port")
    int port = 25565;

    @Setting("network.online-mode")
    boolean onlineMode = true;

    @Setting("network.compression-threshold")
    int compressionThreshold = -1;

    @Setting("network.proxy-mode")
    ProxyMode proxyMode = ProxyMode.VELOCITY;

    @Setting("network.proxy-mode")
    String velocitySecret = "INSERT_SECRET_HERE";

    @Setting("gameplay.difficulty")
    String difficulty = Difficulty.PEACEFUL.name();

    @Setting("gameplay.view-distance.chunks")
    int chunkViewDistance = 5;

    @Setting("gameplay.view-distance.entities")
    int entityViewDistance = 8;

}
