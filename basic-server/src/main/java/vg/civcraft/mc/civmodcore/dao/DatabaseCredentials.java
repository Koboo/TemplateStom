package vg.civcraft.mc.civmodcore.dao;

/**
 * This is a data class representing a set of credentials used for connecting to databases.
 *
 * @author Protonull
 */
public record DatabaseCredentials(String user,
                                  String pass,
                                  String host,
                                  int port,
                                  String driver,
                                  String database,
                                  int poolSize,
                                  long connectionTimeout,
                                  long idleTimeout,
                                  long maxLifetime) {

}
