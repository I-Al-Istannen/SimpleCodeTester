package me.ialistannen.simplecodetester.backend.db.storage;

import java.io.Closeable;
import java.util.Objects;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Record11;
import org.jooq.Record12;
import org.jooq.Record13;
import org.jooq.Record14;
import org.jooq.Record15;
import org.jooq.Record16;
import org.jooq.Record17;
import org.jooq.Record18;
import org.jooq.Record19;
import org.jooq.Record2;
import org.jooq.Record20;
import org.jooq.Record21;
import org.jooq.Record22;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Record5;
import org.jooq.Record6;
import org.jooq.Record7;
import org.jooq.Record8;
import org.jooq.Record9;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectField;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWhereStep;
import org.jooq.Table;

/**
 * Allows read only access to a database.
 */
public class DBReadAccess implements Closeable {

	protected final DSLContext ctx;

	public DBReadAccess(DSLContext ctx) {
		this.ctx = Objects.requireNonNull(ctx);
	}

	@Override
	public void close() {
	}

	public DSLContext dsl() {
		return ctx;
	}

	public boolean fetchExists(Select<?> query) {
		return ctx.fetchExists(query);
	}

	public <R extends Record> SelectWhereStep<R> selectFrom(Table<R> table) {
		return ctx.selectFrom(table);
	}
}
