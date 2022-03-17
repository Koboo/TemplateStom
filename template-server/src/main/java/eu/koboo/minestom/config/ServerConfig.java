package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProviderConfig;
import eu.koboo.minestom.api.config.ProviderConfig.Value;
import java.util.Locale;

public record ServerConfig(String host, int port, boolean onlineMode,
                           ProxyMode proxyMode, String proxySecret) {

    private static final Value<String> hostValue = ProviderConfig.value("host", "0.0.0.0",
            "The preferred host address of the server-socket");
    private static final Value<Integer> portValue = ProviderConfig.value("port", 25565,
            "The preferred port of the server-socket");
    private static final Value<Boolean> onlineModeValue = ProviderConfig.value("online-mode", true,
            "Select if you want to enable online-mode.");

    private static final Value<String> proxyModeValue = ProviderConfig.value("proxy-mode", "NONE",
            "Select your preferred proxy-mode: NONE, BUNGEECORD, VELOCITY");
    private static final Value<String> proxySecretValue = ProviderConfig.value("proxy-secret", "",
            "If you selected proxy-mode VELOCITY enter your secret here.");

    public static ServerConfig load() {
        ProviderConfig config = ProviderConfig.of("server_settings.yml")
                .apply(hostValue)
                .apply(portValue)
                .apply(onlineModeValue)
                .apply(proxyModeValue)
                .apply(proxySecretValue);

        return new ServerConfig(
                config.getOf(hostValue),
                config.getOf(portValue),
                config.getOf(onlineModeValue),
                ProxyMode.valueOf(config.getOf(proxyModeValue).toUpperCase(Locale.ROOT)),
                config.getOf(proxySecretValue)
        );
    }
}
