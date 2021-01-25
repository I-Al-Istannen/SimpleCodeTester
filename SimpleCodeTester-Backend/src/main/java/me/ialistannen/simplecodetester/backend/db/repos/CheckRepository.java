package me.ialistannen.simplecodetester.backend.db.repos;

import static java.util.stream.Collectors.toMap;
import static org.jooq.codegen.db.tables.CodeCheck.CODE_CHECK;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.storage.DBReadAccess;
import me.ialistannen.simplecodetester.backend.db.storage.DBWriteAccess;
import me.ialistannen.simplecodetester.backend.db.storage.DatabaseStorage;
import me.ialistannen.simplecodetester.checks.CheckType;
import org.jooq.codegen.db.tables.records.CodeCheckRecord;
import org.springframework.stereotype.Service;

@Service
public class CheckRepository {

  private final DatabaseStorage databaseStorage;
  private final UserRepository userRepository;
  private final CheckCategoryRepository checkCategoryRepository;

  public CheckRepository(DatabaseStorage databaseStorage, UserRepository userRepository,
      CheckCategoryRepository checkCategoryRepository) {
    this.databaseStorage = databaseStorage;
    this.userRepository = userRepository;
    this.checkCategoryRepository = checkCategoryRepository;
  }

  public List<CodeCheck> findAll() {
    return databaseStorage.acquireReadTransaction(db -> {
      Map<String, User> userMap = userRepository.findAll().stream()
          .collect(toMap(User::getId, it -> it));

      Map<Integer, CheckCategory> categoryMap = checkCategoryRepository.findAll()
          .stream()
          .collect(toMap(CheckCategory::getId, it -> it));

      return db
          .selectFrom(CODE_CHECK)
          .stream()
          .map(it -> recordToCheck(
              it, userMap.get(it.getCreatorId()), categoryMap.get(it.getCategoryId()))
          )
          .collect(Collectors.toList());
    });
  }

  public int count() {
    try (DBReadAccess db = databaseStorage.acquireReadAccess()) {
      return db.dsl().fetchCount(CODE_CHECK);
    }
  }

  public Optional<CodeCheck> findById(int id) {
    return databaseStorage.acquireReadTransaction(db -> {
      Optional<CodeCheckRecord> recordOpt = db.selectFrom(CODE_CHECK)
          .where(CODE_CHECK.ID.eq(id))
          .fetchOptional();

      if (recordOpt.isEmpty()) {
        return Optional.empty();
      }

      User user = userRepository.findById(recordOpt.get().getCreatorId()).orElseThrow();
      CheckCategory category = checkCategoryRepository
          .findById(recordOpt.get().getCategoryId())
          .orElseThrow();

      return recordOpt.map(record -> recordToCheck(record, user, category));
    });
  }

  private CodeCheck recordToCheck(CodeCheckRecord record, User user, CheckCategory category) {
    return new CodeCheck(
        record.getId(),
        record.getText(),
        user,
        category,
        record.getCreationTime(),
        record.getUpdateTime(),
        record.getApproved(),
        CheckType.valueOf(record.getCheckType()),
        record.getName()
    );
  }

  public CodeCheck save(CodeCheck check) {
    return databaseStorage.acquireWriteTransaction(db -> {
      if (db.fetchExists(db.selectFrom(CODE_CHECK).where(CODE_CHECK.ID.eq(check.getId())))) {
        return updateCheck(check, db);
      } else {
        return insertNewCheck(check, db);
      }
    });
  }

  public int deleteCodeCheckById(int id) {
    try (DBWriteAccess db = databaseStorage.acquireWriteAccess()) {
      return db.deleteFrom(CODE_CHECK)
          .where(CODE_CHECK.ID.eq(id))
          .execute();
    }
  }

  public void deleteAll() {
    try (DBWriteAccess db = databaseStorage.acquireWriteAccess()) {
      db.deleteFrom(CODE_CHECK).execute();
    }
  }

  private CodeCheck insertNewCheck(CodeCheck check, DBWriteAccess db) {
    CodeCheckRecord record = db.insertInto(CODE_CHECK)
        .set(CODE_CHECK.TEXT, check.getText())
        .set(CODE_CHECK.CREATOR_ID, check.getCreator().getId())
        .set(CODE_CHECK.CATEGORY_ID, check.getCategory().getId())
        .set(CODE_CHECK.CREATION_TIME, check.getCreationTime())
        .set(CODE_CHECK.UPDATE_TIME, check.getUpdateTime().orElse(null))
        .set(CODE_CHECK.APPROVED, check.isApproved())
        .set(CODE_CHECK.CHECK_TYPE, check.getCheckType().name())
        .set(CODE_CHECK.NAME, check.getName())
        .returning()
        .fetchOne();

    return recordToCheck(record, check.getCreator(), check.getCategory());
  }

  private CodeCheck updateCheck(CodeCheck check, DBWriteAccess db) {
    db.update(CODE_CHECK)
        .set(new CodeCheckRecord(
            check.getId(),
            check.isApproved(),
            check.getCheckType().name(),
            check.getCreationTime(),
            check.getName(),
            check.getText(),
            check.getUpdateTime().orElse(null),
            check.getCategory().getId(),
            check.getCreator().getId()
        ))
        .where(CODE_CHECK.ID.eq(check.getId()))
        .returning()
        .fetchOne();

    return check;
  }
}
