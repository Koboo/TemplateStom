package vg.civcraft.mc.civmodcore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Plugins should replace their custom Database handlers with an instance of ManagedDatasource.
 *
 * See the {@link #ManagedDatasource} constructor for details on how to use the ManagedDatasource.
 *
 * To convert existing plugins, do the following:
 *
 * <ol><li> Take existing database version code and refactor it.
 * <ol><li> Any CREATE, UPDATE, ALTER, or similar statements, convert
 * to a List of Strings and pass them into ManagedDatasource as a Migration using
 * {@link #registerMigration(int, boolean, String...)}</li>
 * <li>Find your prepared statements. Convert the string resources as static final in your plugin's DAO layer.</li>
 * <li>Remove any "is database alive" check code. It's not needed.</li>
 * <li>Remove any version management code that remains</li>
 * <li>DO react to the results of the {@link #updateDatabase} call.
 * <ol><li> If false is returned, terminate your plugin. <li>
 * <li>If false is returned and your plugin is critical to a host of other plugins, terminate the server.</li>
 * <li>If an Exception is thrown, I strongly recommend you consider it a "false" return value and react
 * accordingly.</li></ol></li>
 * <li>Note: Create a "first" migration at index -1 that ignores errors and copies
 * any "current" migration state data from the db_version or equivalent table into the <code>managed_plugin_data</code>
 * table.</li></ol></li>
 * <li>Throughout your plugin, ensure that PreparedStatements are "created" new each request and against a newly
 * retrieved Connection (using {@link #getConnection()} of this class). Don't worry about PrepareStatements. The driver
 * will manage caching them efficiently.</li>
 * <li>Make sure you release Connections using {@link Connection#close()} as soon as you can
 * (when done with them).
 * <ol><li>Don't hold on to Connections.</li>
 * <li>Close them.</li>
 * <li>Use "try-with-resources" where-ever possible so that they are auto-closed.</li></ol></li>
 * <li>If you have loops to insert a bunch of similar records, convert
 * it to a batch. Find instructions in {@link #ManagedDatasource}.</li>
 * <li>If you have special needs like atomic
 * multi-statement, do all your work on a single Connection and return it to a clean state when you are done. (turn
 * auto-commit back on, ensure all transactions are committed, etc.)</li></ol>
 *
 * That should cover most cases. Note that points 2 and 3 are critical. Point 1 is required. Point 4 and 5 are highly
 * recommended.
 *
 * @author ProgrammerDan (refactored by Protonull)
 */
public final class ManagedDatasource {

	private static final Logger LOGGER = MinecraftServer.LOGGER;

    private static final String CREATE_MIGRATIONS_TABLE = """
    CREATE TABLE IF NOT EXISTS managed_plugin_data (
        managed_id BIGINT NOT NULL AUTO_INCREMENT,
        plugin_name VARCHAR(120) NOT NULL,
        management_began TIMESTAMP NOT NULL DEFAULT NOW(),
        current_migration_number INT NOT NULL,
        last_migration TIMESTAMP,
        CONSTRAINT pk_managed_plugin_data PRIMARY KEY (managed_id),
        CONSTRAINT uniq_managed_plugin UNIQUE (plugin_name),
        INDEX idx_managed_plugin USING BTREE (plugin_name)
    );
    """;
	private static final String CREATE_LOCK_TABLE = """
    CREATE TABLE IF NOT EXISTS managed_plugin_locks (
        plugin_name VARCHAR(120) NOT NULL,
        lock_time TIMESTAMP NOT NULL DEFAULT NOW(),
        CONSTRAINT pk_managed_plugin_locks PRIMARY KEY (plugin_name)
    );
    """;
	private static final String CHECK_LAST_MIGRATION = """
    SELECT current_migration_number FROM managed_plugin_data WHERE plugin_name = ?;
    """;
	private static final String RECORD_MIGRATION = """
    INSERT INTO managed_plugin_data (
        plugin_name,
        current_migration_number,
        last_migration
    )
    VALUES (
        ?,
        ?,
        NOW()
    )
    ON DUPLICATE KEY UPDATE
        plugin_name = VALUES(plugin_name),
        current_migration_number = VALUES(current_migration_number),
        last_migration = VALUES(last_migration);
    """;
	private static final String CLEANUP_LOCK_TABLE = """
    DELETE FROM managed_plugin_locks WHERE lock_time <= TIMESTAMPADD(HOUR, -8, NOW());
    """;
	private static final String ACQUIRE_LOCK = """
    INSERT IGNORE INTO managed_plugin_locks (plugin_name) VALUES (?);
    """;
	private static final String RELEASE_LOCK = """
    DELETE FROM managed_plugin_locks WHERE plugin_name = ?;
    """;
	private static final long MAX_WAIT_FOR_LOCK = 600_000L;
	private static final long WAIT_PERIOD = 500L;

	private final Extension extension;
    private final ConnectionPool connections;
	private final ExecutorService postExecutor;
	private final TreeMap<Integer, Migration> migrations;
	private int firstMigration;
	private int lastMigration;

	/**
	 * Create a new ManagedDatasource.
	 *
	 * @param extension The plugin whose database is being managed.
	 * @param credentials The credentials to connect to the database with.
	 */
	public ManagedDatasource(@NotNull final Extension extension,
                             @NotNull final DatabaseCredentials credentials) {
		this.extension = Objects.requireNonNull(extension);
		LOGGER.info("Connecting to " + credentials.database() + "@"
				+ credentials.host() + ":" + credentials.port()
				+ " using " + credentials.user());
		this.connections = new ConnectionPool(credentials);
		this.postExecutor = Executors.newSingleThreadExecutor();
		this.migrations = new TreeMap<>();
		this.firstMigration = Integer.MAX_VALUE;
		this.lastMigration = Integer.MIN_VALUE;
		try (final Connection connection = this.connections.getConnection()) {
			try (final Statement statement = connection.createStatement()) {
				statement.executeUpdate(ManagedDatasource.CREATE_MIGRATIONS_TABLE);
			}
			try (final Statement statement = connection.createStatement()) {
				statement.executeUpdate(ManagedDatasource.CREATE_LOCK_TABLE);
			}
		}
		catch (final SQLException thrown) {
			LOGGER.error("Failed to prepare migrations table or register this plugin to it.");
			LOGGER.error("Assuming you provided proper database credentials this is most likely "
					+ "happening, because your mysql install is outdated. We recommend using MariaDB or at least the "
					+ "latest mysql version", thrown);
		}
	}

	/**
	 * Use this to register a migration. After all migrations have been registered, call {@link #updateDatabase()}.
	 *
	 * This is <i>not</i> checked for completeness or accuracy.
	 *
	 * @param id The migration ID -- 0, 1, 2 etc, must be unique.
	 * @param ignoreErrors Indicates if errors in this migration should be ignored.
	 * @param queries The queries to run, in sequence.
	 */
	public void registerMigration(final int id,
                                  final boolean ignoreErrors,
                                  final String... queries) {
		registerMigration(id, ignoreErrors, null, queries);
	}

	/**
	 * Use this to register a migration. After all migrations have been registered, call {@link #updateDatabase()}.
	 *
	 * This is <i>not</i> checked for completeness or accuracy.
	 *
	 * @param id The migration ID -- 0, 1, 2 etc, must be unique.
	 * @param ignoreErrors Indicates if errors in this migration should be ignored.
	 * @param callback An optional callback that'll run after the migration has completed.
	 * @param queries The queries to run, in sequence.
	 */
	public void registerMigration(final int id,
                                  final boolean ignoreErrors,
                                  final Callable<Boolean> callback,
                                  final String... queries) {
		this.migrations.put(id, new Migration(ignoreErrors, callback, queries));
		if (id > this.lastMigration) {
            this.lastMigration = id;
		}
		if (id < this.firstMigration) {
            this.firstMigration = id;
		}
	}

	/**
	 * This method should be called by your plugin after all migrations have been registered. It applies the migrations
	 * if necessary in a "multi-tenant" safe way via a soft-lock. Locks have a maximum duration currently set to 8
	 * hours, but realistically they will be very short. For multi-tenant updates all servers should gracefully wait in
	 * line.
	 *
	 * 1. Attempts to get a lock for migrations for this plugin. 2. If unsuccessful, periodically check the lock for
	 * release. a. Once released, restart at 1. b. If Timeout occurs, return "false". 3. If successful, check for
	 * current update level. a. If no record exists, start with first migration, and apply in sequence from first to
	 * last, updating the migration management table along the way b. If a record exists, read which migration was
	 * completed last i. If identical to "highest" registered migration level, do nothing. ii. If less then "highest"
	 * registered migration level, get the tailset of migrations "after" the last completed level, and run. 4. If no
	 * errors occurred, or this migration has errors marked ignored, return true. 5. If errors, return false. 6. In
	 * either case, release the lock.
	 *
	 * @return As described in the algorithm above, returns true if no errors or all ignored; or false if unable to
	 *         start migration in a timely fashion or errors occurred.
	 */
	public boolean updateDatabase() {
		try {
			checkWaitLock();
		}
		catch (final SQLException thrown) {
			LOGGER.error("An uncorrectable SQL error was encountered!", thrown);
			return false;
		}
		catch (final TimeoutException thrown) {
			LOGGER.error("Unable to acquire a lock!", thrown);
			return false;
		}
		// Now check update level, etc.
		int currentLevel = this.migrations.firstKey() - 1;
		try (final Connection connection = getConnection();
             final PreparedStatement statement = connection.prepareStatement(CHECK_LAST_MIGRATION)) {
			statement.setString(1, this.extension.getOrigin().getName());
			try (final ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					currentLevel = set.getInt(1);
				} // else we aren't tracked yet!
			}
		}
		catch (final SQLException thrown) {
			LOGGER.error("Unable to check last migration!", thrown);
			releaseLock();
			return false;
		}
        final NavigableMap<Integer, Migration> newApply = this.migrations.tailMap(currentLevel, false);
		try {
			if (newApply.size() > 0) {
				LOGGER.info(String.format("%s database is behind, %s migrations found",
                        this.extension.getOrigin().getName(), newApply.size()));
				if (doMigrations(newApply)) {
					LOGGER.info(this.extension.getOrigin().getName() + " fully migrated.");
				}
				else {
					LOGGER.warn(this.extension.getOrigin().getName() + " failed to apply updates.");
					return false;
				}
			}
			else {
				LOGGER.info(this.extension.getOrigin().getName() + " database is up to date.");
			}
			return true;
		}
		catch (final Throwable thrown) {
			LOGGER.warn(this.extension.getOrigin().getName() + " failed to apply updates for some reason...");
			LOGGER.error("Full exception: ", thrown);
			return false;
		}
		finally {
			releaseLock();
		}
	}

	private boolean doMigrations(final NavigableMap<Integer, Migration> migrations) {
		try {
			for (final Integer id : migrations.keySet()) {
				LOGGER.info("Migration " +  id + " ] Applying");
                final Migration migration = migrations.get(id);
				if (migration == null) {
					continue; // huh?
				}
				if (doMigration(id, migration.migrations, migration.ignoreErrors, migration.postMigration)) {
					LOGGER.info("Migration " +  id + " ] Successful");
					try (final Connection connection = getConnection();
                         final PreparedStatement statement = connection.prepareStatement(RECORD_MIGRATION);) {
						statement.setString(1, this.extension.getOrigin().getName());
						statement.setInt(2, id);
						if (statement.executeUpdate() < 1) {
							LOGGER.warn("Might not have recorded migration " + id + " occurrence successfully.");
						}
					}
					catch (final SQLException thrown) {
						LOGGER.warn("Failed to record migration " + id + " occurrence successfully.");
						LOGGER.error("Full Error: ", thrown);
						return false;
					}
				}
				else {
					LOGGER.info("Migration " +  id + " ] Failed");
					return false;
				}
			}
			return true;
		}
		catch (final Throwable thrown) {
			LOGGER.error("Unexpected failure during migrations", thrown);
			return false;
		}
	}

	private boolean doMigration(final Integer migration,
                                final List<String> queries,
                                final boolean ignoreErrors,
                                final Callable<Boolean> post) {
		try (final Connection connection = getConnection()) {
			for (final String query : queries) {
				try (final Statement statement = connection.createStatement()) {
					statement.executeUpdate(query);
					if (!ignoreErrors) { // if we ignore errors we totally ignore warnings.
						SQLWarning warning = statement.getWarnings();
						while (warning != null) {
							LOGGER.warn("Migration " + migration + " ] Warning: " + warning.getMessage());
							// TODO: add verbose check
							warning = warning.getNextWarning();
						}
					}
				}
				catch (final SQLException thrown) {
					if (ignoreErrors) {
						LOGGER.warn("Migration " + migration + " ] Ignoring error: " + thrown.getMessage());
					}
					else {
						throw thrown;
					}
				}
			}
		}
		catch (final SQLException thrown) {
			if (ignoreErrors) {
				LOGGER.warn("Migration " + migration + " ] Ignoring error: " + thrown.getMessage());
			}
			else {
				LOGGER.warn("Migration " + migration + " ] Failed migration: " + thrown.getMessage());
				LOGGER.error("Full Error: ", thrown);
				return false;
			}
		}
		if (post != null) {
            final Future<Boolean> doing = this.postExecutor.submit(post);
			try {
				if (doing.get()) {
					LOGGER.info("Migration " + migration + " ] Post Call Complete");
				}
				else {
					if (ignoreErrors) {
						LOGGER.warn("Migration " + migration + " ] Post Call indicated failure; ignored.");
					}
					else {
						LOGGER.warn("Migration " + migration + " ] Post Call failed!");
						return false;
					}
				}
			}
			catch (final Throwable thrown) {
				if (ignoreErrors) {
					LOGGER.warn("Migration " + migration + " ] Post Call indicated failure; ignored: " +
							thrown.getMessage());
				}
				else {
					LOGGER.warn("Migration " + migration + " ] Post Call failed!");
					LOGGER.error("Full Error: ", thrown);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if this plugin is already managed by the ManagedDatasource infrastructure or not.
	 *
	 * @return Returns true if the plugin has an entry in the migrations table; false for any other outcome.
	 */
	public boolean isManaged() {
		try (final Connection connection = getConnection();
             final PreparedStatement statement = connection.prepareStatement(CHECK_LAST_MIGRATION)) {
			statement.setString(1, extension.getOrigin().getName());
			try (final ResultSet set = statement.executeQuery()) {
				return set.next();
			}
		}
		catch (final SQLException ignored) {
			return false;
		}
	}

	/**
	 * This attempts to acquire a lock every WAIT_PERIOD milliseconds, up to MAX_WAIT_FOR_LOCK milliseconds.
	 *
	 * If max wait is exhausted, throws a TimeoutException.
	 *
	 * If a <i>real</i> error (not failure to insert) is encountered, stops trying and throws that error.
	 *
	 * Otherwise, returns true when lock is acquired.
	 *
	 * @return true when lock is acquired, or exception otherwise
	 * @throws TimeoutException If lock isn't acquired by max wait time.
	 * @throws SQLException If an exception is encountered
	 */
	private boolean checkWaitLock() throws TimeoutException, SQLException {
		/* First, cleanup old locks if any */
		try (final Connection connection = getConnection();
             final Statement cleanup = connection.createStatement()) {
			cleanup.executeUpdate(CLEANUP_LOCK_TABLE);
		}
		catch (final SQLException thrown) {
			LOGGER.error("Unable to cleanup old locks, error encountered!");
			throw thrown;
		}
		/* Now get our own lock */
        final long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < MAX_WAIT_FOR_LOCK) {
			try (final Connection connection = getConnection();
                 final PreparedStatement tryAcquire = connection.prepareStatement(ACQUIRE_LOCK)) {
				tryAcquire.setString(1, this.extension.getOrigin().getName());
                final int hasLock = tryAcquire.executeUpdate();
				if (hasLock > 0) {
					LOGGER.info("Lock acquired, proceeding.");
					return true;
				}
			}
			catch (final SQLException thrown) {
				LOGGER.error("Unable to acquire a lock, error encountered!");
				// let the exception continue so we return right away; only errors we'd encounter here are terminal.
				throw thrown;
			}
			if (System.currentTimeMillis() - start > MAX_WAIT_FOR_LOCK) {
				break;
			}
			try {
				Thread.sleep(WAIT_PERIOD);
			}
			catch (final InterruptedException ignored) {
				// Someone wants us to check right away.
			}
		}
		throw new TimeoutException("We were unable to acquire a lock in the time allowed");
	}

	private void releaseLock() {
		try (final Connection connection = getConnection();
             final PreparedStatement release = connection.prepareStatement(RELEASE_LOCK)) {
			release.setString(1, this.extension.getOrigin().getName());
            final int releaseLock = release.executeUpdate();
			if (releaseLock < 1) {
				LOGGER.warn("Attempted to release a lock, already released.");
			}
			else {
				LOGGER.info("Lock released.");
			}
		}
		catch (final SQLException thrown) {
			LOGGER.warn("Attempted to release lock; failed. This may interrupt startup for other " +
							"servers working against this database.", thrown);
		}
	}

	/**
	 * Passthrough; gets a connection from the underlying ConnectionPool. Simply close() it when done.
	 *
	 * This method _could_ briefly block while waiting for a connection. Keep this in mind.
	 *
	 * @return Returns a connection from the pool.
	 * @throws SQLException If the pool has gone away, database is not connected, or other error has occurred.
	 */
	public Connection getConnection() throws SQLException {
		return this.connections.getConnection();
	}

	/**
	 * Passthrough; closes the underlying pool. Cannot be undone.
	 *
	 * @throws SQLException Something went horribly wrong.
	 */
	public void close() throws SQLException {
		this.connections.close();
	}

	private static class Migration {
		public List<String> migrations;
		public boolean ignoreErrors;
		public Callable<Boolean> postMigration;
		public Migration(final boolean ignoreErrors,
                         final Callable<Boolean> postMigration,
                         final String... migrations) {
			this.migrations = List.of(migrations);
			this.ignoreErrors = ignoreErrors;
			this.postMigration = postMigration;
		}
	}

}
