package eu.koboo.minestom.api.config;

import net.minestom.server.entity.GameMode;
import net.minestom.server.world.Difficulty;

public record ServerConfig(String host, int port,
                           boolean onlineMode,
                           Difficulty difficulty,
                           GameMode gameMode,

                           ProxyMode proxyMode,
                           String velocitySecret,

                           int packetRateLimit,
                           int maxPacketSize,
                           int compressionThreshold,

                           int chunkViewDistance,
                           int entityViewDistance
                           ) {

}
