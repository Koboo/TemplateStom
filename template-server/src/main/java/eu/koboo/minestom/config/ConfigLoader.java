package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProxyMode;
import eu.koboo.minestom.api.config.ServerConfig;
import java.io.IOException;
import java.util.Locale;
import org.simpleyaml.configuration.file.YamlFile;

public class ConfigLoader {

  public static ServerConfig loadConfig() {
    YamlFile cfg = new YamlFile("server_config.yml");

    cfg.options().copyDefaults(true);

    try {
      if(!cfg.exists()) {
        cfg.createNewFile();

        defaultValue(cfg, "server.host", "0.0.0.0", "The host address of the server");
        defaultValue(cfg, "server.port", 25565, "The listening port of the server");
        defaultValue(cfg, "server.online-mode", true, "Toggle online-mode (mojang authentication) of the server");
        defaultValue(cfg, "proxy.proxy-mode", ProxyMode.NONE.name(), "Toggle proxy-mode of the server (options: NONE, BUNGEECORD, VELOCITY)");
        defaultValue(cfg, "proxy.velocity-secret", "", "Set your velocity-secret");

        cfg.save();
      }
      cfg.loadWithComments();
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong, while loading server_config.yml: ", e);
    }

    String host = cfg.getString("server.host");
    int port = cfg.getInt("server.port");
    boolean onlineMode = cfg.getBoolean("server.online-mode");
    ProxyMode proxyMode = ProxyMode.valueOf(cfg.getString("proxy.proxy-mode").toUpperCase(Locale.ROOT));
    String velocitySecret = cfg.getString("proxy.velocity-secret");

    return new ServerConfig(host, port, onlineMode, proxyMode, velocitySecret);
  }

  private static void defaultValue(YamlFile yamlFile, String key, Object value, String comment) {
    yamlFile.addDefault(key, value);
    yamlFile.setComment(key, comment);
  }

}
