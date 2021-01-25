package me.ialistannen.simplecodetester.backend.db.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Metrics;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.ialistannen.simplecodetester.backend.util.CheckedConsumer;
import me.ialistannen.simplecodetester.backend.util.CheckedFunction;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteDataSource;

/**
 * Provides access to a database.
 */
public class DatabaseStorage {

	private final DSLContext context;
	private final Lock writeLock = new ReentrantLock();

	/**
	 * Initializes the database storage.
	 *
	 * <p> Also performs database migrations, if necessary.
	 *
	 * @param jdbcUrl the jdbc url used to connect to the database
	 */
	public DatabaseStorage(String jdbcUrl) {
		migrate(jdbcUrl);

		SQLiteConfig sqliteConfig = new SQLiteConfig();
		sqliteConfig.enforceForeignKeys(true);
		sqliteConfig.setJournalMode(JournalMode.WAL);

		SQLiteDataSource sqLiteDataSource = new SQLiteDataSource(sqliteConfig);
		sqLiteDataSource.setUrl(jdbcUrl);

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDataSource(sqLiteDataSource);
		hikariConfig.setPoolName("codetester-db-pool");
		hikariConfig.setMetricRegistry(Metrics.globalRegistry);

		HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

		this.context = DSL.using(hikariDataSource, SQLDialect.SQLITE);
	}

	/**
	 * By default, sqlite doesn't check for foreign key constraints. By opening a new db connection
	 * only based on the db url, flyway can take advantage of this and migrations become much more
	 * performant. This does however mean that each migration has to check foreign key consistency
	 * itself using {@code PRAGMA foreign_key_check}.
	 *
	 * @param jdbcUrl the url to the database
	 */
	private void migrate(String jdbcUrl) {
		Flyway flyway = Flyway.configure()
			.dataSource(jdbcUrl, "", "")
			.load();

		flyway.migrate();
	}

	/**
	 * Acquires read access to the database.
	 *
	 * @return a way to interact with the database in a read only way
	 */
	public DBReadAccess acquireReadAccess() {
		return new DBReadAccess(acquireContext());
	}

	/**
	 * Acquires read and write access to the database.
	 *
	 * @return a way to interact with the database
	 */
	public DBWriteAccess acquireWriteAccess() {
		return new DBWriteAccess(acquireContext(), this.writeLock);
	}

	/**
	 * Acquires a transaction that can only read from the database.
	 *
	 * @param handler the handler that is executed within the context of the transaction. The handler
	 * 	will be called once and its return value returned from the transaction.
	 * @param <T> the handler's return type
	 * @return whatever the handler returned
	 */
	public <T> T acquireReadTransaction(CheckedFunction<DBReadAccess, T, Throwable> handler) {
		AtomicReference<T> result = new AtomicReference<>();

		try (final DBReadAccess db = acquireReadAccess()) {
			db.dsl().transaction(cfg -> {
				try (DBReadAccess inTransactionDB = new DBReadAccess(cfg.dsl())) {
					result.set(handler.accept(inTransactionDB));
				}
			});
		}

		return result.get();
	}

	/**
	 * Acquires a transaction that can only read from the database.
	 *
	 * @param handler the handler that is executed within the context of the transaction. It has no
	 * 	return value.
	 */
	public void acquireReadTransaction(CheckedConsumer<DBReadAccess, Throwable> handler) {
		acquireWriteTransaction(db -> {
			handler.accept(db);
			return null;
		});
	}

	/**
	 * Acquires a transaction that can read and write to the database.
	 *
	 * @param handler the handler that is executed within the context of the transaction. The handler
	 * 	will be called once and its return value returned from the transaction.
	 * @param <T> the handler's return type
	 * @return whatever the handler returned
	 */
	public <T> T acquireWriteTransaction(CheckedFunction<DBWriteAccess, T, Throwable> handler) {
		AtomicReference<T> result = new AtomicReference<>();

		try (final DBWriteAccess db = acquireWriteAccess()) {
			db.dsl().transaction(cfg -> {
				try (DBWriteAccess inTransactionDB = new DBWriteAccess(cfg.dsl(), this.writeLock)) {
					result.set(handler.accept(inTransactionDB));
				}
			});
		}

		return result.get();
	}

	/**
	 * Acquires a transaction that can read and write to the database.
	 *
	 * @param handler the handler that is executed within the context of the transaction. It has no
	 * 	return value.
	 */
	public void acquireWriteTransaction(CheckedConsumer<DBWriteAccess, Throwable> handler) {
		acquireWriteTransaction(db -> {
			handler.accept(db);
			return null;
		});
	}

	/**
	 * @return a {@link DSLContext} instance providing jooq functionality along with a connection to
	 * 	the database
	 */
	private DSLContext acquireContext() {
		return this.context;
	}

}
