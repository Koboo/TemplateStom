package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProxyMode;
import eu.koboo.minestom.api.config.ServerConfig;
import java.io.IOException;
import java.util.Locale;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.world.Difficulty;
import org.simpleyaml.configuration.file.YamlFile;

public class ConfigLoader {

  public static ServerConfig loadConfig() {
    YamlFile cfg = new YamlFile("server_config.yml");

    cfg.options().copyDefaults(true);

    try {
      if(!cfg.exists()) {
        cfg.createNewFile();
      }
      cfg.loadWithComments();

      defaultValue(cfg, "server.host", "0.0.0.0", "The host address of the server");
      defaultValue(cfg, "server.port", 25565, "The listening port of the server");
      defaultValue(cfg, "server.online-mode", true, "Toggle online-mode (mojang authentication) of the server");
      defaultValue(cfg, "server.difficulty", Difficulty.NORMAL.name(), "Toggle difficulty (options: PEACEFUL, EASY, NORMAL, HARD)");
      defaultValue(cfg, "server.gamemode", GameMode.SURVIVAL.name(), "Toggle gamemode (options: CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR)");

      defaultValue(cfg, "proxy.proxy-mode", ProxyMode.NONE.name(), "Toggle proxy-mode (options: NONE, BUNGEECORD, VELOCITY)");
      defaultValue(cfg, "proxy.velocity-secret", "", "Set your velocity-secret (Do not share that!)");

      defaultValue(cfg, "packets.rate-limit", 450, "Set the rate-limit of packets/second for the clients (0 disables rate-limit)");
      defaultValue(cfg, "packets.max-size", 2097151, "Set the max-size of packets from the clients (maximum is 2097151 bytes)");
      defaultValue(cfg, "packets.compression-threshold", "", "Set the compression-threshold of packets (0 disables compression)");

      defaultValue(cfg, "view-distance.chunks", 10, "Set the view-distance of chunks (range between 2 and 32)");
      defaultValue(cfg, "view-distance.entities", 10, "Set the view-distance of entities (range between 2 and 32)");

      cfg.save();

      String host = cfg.getString("server.host");
      int port = cfg.getInt("server.port");
      boolean onlineMode = cfg.getBoolean("server.online-mode");
      Difficulty difficulty = Difficulty.valueOf(cfg.getString("server.difficulty").toUpperCase(
          Locale.ROOT));
      GameMode gameMode = GameMode.valueOf(cfg.getString("server.gamemode").toUpperCase(Locale.ROOT));

      ProxyMode proxyMode = ProxyMode.valueOf(cfg.getString("proxy.proxy-mode").toUpperCase(Locale.ROOT));
      String velocitySecret = cfg.getString("proxy.velocity-secret");

      int rateLimit = cfg.getInt("packets.rate-limit");
      int maxPacketSize = cfg.getInt("packets.max-size");
      int compressionThreshold = cfg.getInt("packets.compression-threshold");

      int viewDistanceChunks = cfg.getInt("view-distance.chunks");
      int viewDistanceEntities = cfg.getInt("view-distance.entities");

      return new ServerConfig(
          host, port, onlineMode, difficulty, gameMode,
          proxyMode, velocitySecret,
          rateLimit, maxPacketSize, compressionThreshold,
          viewDistanceChunks, viewDistanceEntities
      );
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong, while loading server_config.yml: ", e);
    }
  }

  private static void defaultValue(YamlFile yamlFile, String key, Object value, String comment) {
    if(!yamlFile.contains(key)) {
      yamlFile.set(key, value);
      yamlFile.setComment(key, comment);
    }
  }

}
