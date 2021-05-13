package vg.civcraft.mc.civmodcore.dao;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Handy Connection Pool / Database wrapper for use by all plugins.
 *
 * @author ProgrammerDan
 */
public final class ConnectionPool {

	private static final Logger LOGGER = MinecraftServer.LOGGER;

	private final DatabaseCredentials credentials;
	private HikariDataSource datasource;

	/**
	 * Creates a new ConnectionPool based on a given set of credentials. Note that the credentials are not scrutinised,
	 * so you should make sure they're valid, or at least valid enough, otherwise expect exceptions or logger spam.
	 *
	 * @param credentials The credentials to connect with.
	 */
	public ConnectionPool(@NotNull final DatabaseCredentials credentials) {
		this.credentials = Objects.requireNonNull(credentials,
                "Cannot create a ConnectionPool with a null set of credentials.");
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:" + credentials.driver() + "://" + credentials.host() + ":" +
				credentials.port() + "/" + credentials.database());
		config.setConnectionTimeout(credentials.connectionTimeout());
		config.setIdleTimeout(credentials.idleTimeout());
		config.setMaxLifetime(credentials.maxLifetime());
		config.setMaximumPoolSize(credentials.poolSize());
		config.setUsername(credentials.user());
		if (!Strings.isNullOrEmpty(credentials.pass())) {
			config.setPassword(credentials.pass());
		}
		this.datasource = new HikariDataSource(config);
		try (final Connection connection = getConnection();
             final Statement statement = connection.createStatement()) {
			statement.executeQuery("SELECT 1;");
			LOGGER.info("Successfully connected to the database.");
		}
		catch (SQLException exception) {
			LOGGER.error("Unable to connect to the database.");
			exception.printStackTrace();
			this.datasource = null;
		}
	}

	/**
	 * Gets the credentials used for this ConnectionPool.
	 *
	 * @return Returns the credentials being used.
	 */
    @NotNull
	public DatabaseCredentials getCredentials() {
		return this.credentials;
	}

	/**
	 * Gets a single connection from the pool for use. Checks for null database first.
	 *
	 * @return Returns a new connection.
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		available();
		return this.datasource.getConnection();
	}

	/**
	 * Closes all connections and this connection pool.
	 *
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		available();
		this.datasource.close();
		this.datasource = null;
	}

	/**
	 * Quick test; either ends or throws an exception if data source isn't configured.
	 *
	 * @throws SQLException
	 */
	public void available() throws SQLException {
		if (this.datasource == null) {
			throw new SQLException("No Datasource Available");
		}
	}

	/**
	 * Available for direct use within this package, use the provided public methods for anything else.
	 *
	 * @return Returns the datasource being used.
	 */
	HikariDataSource getHikariDataSource() {
		return datasource;
	}

}
