package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProviderConfig;
import eu.koboo.minestom.api.config.ProviderConfig.Value;

public record ServerConfig(String host, int port,
                           boolean optifineSupport) {

    private static final Value<String> hostValue = ProviderConfig.value("host", "0.0.0.0",
            "The preferred host address of the server-socket");
    private static final Value<Integer> portValue = ProviderConfig.value("port", 25565,
            "The preferred port of the server-socket");
    private static final Value<Boolean> optifineSupportValue = ProviderConfig.value("optifine-support", true,
            "Select if you want to enable optifine-support.");

    public static ServerConfig load() {
        ProviderConfig config = ProviderConfig.of("configs/server_settings.yml")
                .apply(hostValue)
                .apply(portValue)
                .apply(optifineSupportValue);

        return new ServerConfig(
                config.getOf(hostValue),
                config.getOf(portValue),
                config.getOf(optifineSupportValue)
        );
    }
}
