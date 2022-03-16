package eu.koboo.minestom.config;

import eu.koboo.minestom.api.config.ProviderConfig;
import eu.koboo.minestom.api.config.ProviderConfig.Value;
import java.util.Locale;

public record ProxyConfig(ProxyMode proxyMode,
                          String proxySecret) {

    private static final Value<String> proxyModeValue = ProviderConfig.value("proxy-mode", "NONE",
            "Select your preferred proxy-mode: NONE, BUNGEECORD, VELOCITY");
    private static final Value<String> proxySecretValue = ProviderConfig.value("proxy-secret", "",
            "If you selected proxy-mode VELOCITY enter your secret here.");

    public static ProxyConfig load() {
        ProviderConfig config = ProviderConfig.of("configs/proxy_settings.yml")
                .apply(proxyModeValue)
                .apply(proxySecretValue);

        return new ProxyConfig(
                ProxyMode.valueOf(config.getOf(proxyModeValue).toUpperCase(Locale.ROOT)),
                config.getOf(proxySecretValue)
        );
    }
}
