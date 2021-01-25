package me.ialistannen.simplecodetester.backend.db.repos;

import static org.jooq.codegen.db.tables.CheckCategory.CHECK_CATEGORY;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.storage.DBReadAccess;
import me.ialistannen.simplecodetester.backend.db.storage.DBWriteAccess;
import me.ialistannen.simplecodetester.backend.db.storage.DatabaseStorage;
import org.jooq.codegen.db.tables.records.CheckCategoryRecord;
import org.springframework.stereotype.Service;

@Service
public class CheckCategoryRepository {

  private final DatabaseStorage databaseStorage;

  public CheckCategoryRepository(DatabaseStorage databaseStorage) {
    this.databaseStorage = databaseStorage;
  }

  public List<CheckCategory> findAll() {
    try (DBReadAccess db = databaseStorage.acquireReadAccess()) {
      return db.selectFrom(CHECK_CATEGORY)
          .stream()
          .map(this::recordToCheck)
          .collect(Collectors.toList());
    }
  }

  public Optional<CheckCategory> findById(int id) {
    try (DBReadAccess db = databaseStorage.acquireReadAccess()) {
      return db.selectFrom(CHECK_CATEGORY)
          .where(CHECK_CATEGORY.ID.eq(id))
          .fetchOptional()
          .map(this::recordToCheck);
    }
  }

  public CheckCategory save(String name) {
    try (DBWriteAccess db = databaseStorage.acquireWriteAccess()) {
      CheckCategoryRecord record = db.dsl().insertInto(CHECK_CATEGORY)
          .set(CHECK_CATEGORY.NAME, name)
          .returning()
          .fetchOne();

      return recordToCheck(record);
    }
  }

  public int deleteById(int id) {
    try (DBWriteAccess db = databaseStorage.acquireWriteAccess()) {
      return db.deleteFrom(CHECK_CATEGORY)
          .where(CHECK_CATEGORY.ID.eq(id))
          .execute();
    }
  }

  public Optional<CheckCategory> rename(int id, String newName) {
    try (DBWriteAccess db = databaseStorage.acquireWriteAccess()) {
      return db.update(CHECK_CATEGORY)
          .set(CHECK_CATEGORY.NAME, newName)
          .where(CHECK_CATEGORY.ID.eq(id))
          .returning()
          .fetchOptional()
          .map(this::recordToCheck);
    }
  }

  private CheckCategory recordToCheck(CheckCategoryRecord record) {
    return new CheckCategory(record.getId(), record.getName());
  }
}
