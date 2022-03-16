package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProviderConfig;
import eu.koboo.minestom.api.config.ProviderConfig.Value;
import java.util.Locale;

public record ServerConfig(String host, int port,
                           ProxyMode proxyMode,
                           String proxySecret, boolean optifineSupport) {

    private static final Value<String> hostValue = ProviderConfig.value("host", "0.0.0.0",
            "The preferred host address of the server-socket");
    private static final Value<Integer> portValue = ProviderConfig.value("port", 25565,
            "The preferred port of the server-socket");
    private static final Value<String> proxyModeValue = ProviderConfig.value("proxy-mode", "NONE",
            "Select your preferred proxy-mode: NONE, BUNGEECORD, VELOCITY");
    private static final Value<String> proxySecretValue = ProviderConfig.value("proxy-secret", "",
            "If you selected proxy-mode VELOCITY enter your secret here.");
    private static final Value<Boolean> optifineSupportValue = ProviderConfig.value("optifine-support", true,
            "Select if you want to enable optifine-support.");

    public static ServerConfig load() {
        ProviderConfig config = ProviderConfig.of("configs/server.yml")
                .apply(hostValue)
                .apply(portValue)
                .apply(proxyModeValue)
                .apply(proxySecretValue)
                .apply(optifineSupportValue);

        return new ServerConfig(
                config.getOf(hostValue),
                config.getOf(portValue),
                ProxyMode.valueOf(config.getOf(proxyModeValue).toUpperCase(Locale.ROOT)),
                config.getOf(proxySecretValue),
                config.getOf(optifineSupportValue)
        );
    }
}
