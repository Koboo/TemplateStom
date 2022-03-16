package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProviderConfig;
import eu.koboo.minestom.api.config.ProviderConfig.Value;

public record QueryConfig(boolean enable,
                          int port) {

    private static final Value<Boolean> enableValue = ProviderConfig.value("enable-query", false,
            "Select if you want to enable the query-socket");
    private static final Value<Integer> portValue = ProviderConfig.value("port", 25567,
            "The preferred port of the query-socket");

    public static QueryConfig load() {
        ProviderConfig config = ProviderConfig.of("configs/query_settings.yml")
                .apply(enableValue)
                .apply(portValue);

        return new QueryConfig(
                config.getOf(enableValue),
                config.getOf(portValue)
        );
    }
}
