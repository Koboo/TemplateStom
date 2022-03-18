package eu.koboo.minestom.api.config;

public record ServerConfig(String host, int port, boolean onlineMode,
                           ProxyMode proxyMode,
                           String velocitySecret) {

}
