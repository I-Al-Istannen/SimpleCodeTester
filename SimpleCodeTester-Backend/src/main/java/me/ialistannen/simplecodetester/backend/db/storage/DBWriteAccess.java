package me.ialistannen.simplecodetester.backend.db.storage;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import org.jooq.DSLContext;
import org.jooq.DeleteWhereStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdateSetFirstStep;

/**
 * Allows read and write access to a database.
 */
public class DBWriteAccess extends DBReadAccess {

  private final Lock lock;
  private boolean closed = false;

  public DBWriteAccess(DSLContext ctx, Lock lock) {
    super(ctx);
    this.lock = Objects.requireNonNull(lock);
    this.lock.lock();
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      this.closed = true;
      this.lock.unlock();
    }
  }

  private void checkNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("write access already closed");
    }
  }

  public <R extends Record> DeleteWhereStep<R> deleteFrom(Table<R> table) {
    checkNotClosed();
    return ctx.deleteFrom(table);
  }

  public <R extends Record> UpdateSetFirstStep<R> update(Table<R> table) {
    checkNotClosed();
    return ctx.update(table);
  }

  public <R extends Record> InsertSetStep<R> insertInto(Table<R> table) {
    checkNotClosed();
    return ctx.insertInto(table);
  }

}
