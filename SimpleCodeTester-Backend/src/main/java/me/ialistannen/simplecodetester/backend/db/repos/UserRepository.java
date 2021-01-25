package me.ialistannen.simplecodetester.backend.db.repos;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.jooq.codegen.db.tables.User.USER;
import static org.jooq.codegen.db.tables.UserAuthorities.USER_AUTHORITIES;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.storage.DBReadAccess;
import me.ialistannen.simplecodetester.backend.db.storage.DBWriteAccess;
import me.ialistannen.simplecodetester.backend.db.storage.DatabaseStorage;
import org.jooq.codegen.db.tables.records.UserAuthoritiesRecord;
import org.jooq.codegen.db.tables.records.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class UserRepository {

  private final DatabaseStorage databaseStorage;

  public UserRepository(DatabaseStorage databaseStorage) {
    this.databaseStorage = databaseStorage;
  }

  public List<User> findAll() {
    return databaseStorage.acquireReadTransaction(db -> {
      Map<String, List<String>> authoritiesMap = db.selectFrom(USER_AUTHORITIES)
          .stream()
          .collect(
              groupingBy(
                  UserAuthoritiesRecord::getUserId,
                  mapping(UserAuthoritiesRecord::getAuthorities, toList())
              )
          );
      return db.selectFrom(USER)
          .stream()
          .map(it -> new User(
              it.getId(),
              it.getName(),
              it.getPasswordHash(),
              it.getEnabled(),
              authoritiesMap.getOrDefault(it.getId(), List.of())
          ))
          .collect(toList());
    });
  }

  public void deleteAll() {
    try (DBWriteAccess db = databaseStorage.acquireWriteAccess()) {
      db.deleteFrom(USER)
          .execute();
    }
  }

  public boolean existsUserById(String id) {
    try (DBReadAccess db = databaseStorage.acquireReadAccess()) {
      return db.fetchExists(
          db.selectFrom(USER)
              .where(USER.ID.eq(id))
      );
    }
  }

  public Optional<User> findById(String id) {
    return databaseStorage.acquireReadTransaction(db -> {
      List<String> authorities = db.selectFrom(USER_AUTHORITIES)
          .where(USER_AUTHORITIES.USER_ID.eq(id))
          .stream()
          .map(UserAuthoritiesRecord::getAuthorities)
          .collect(toList());

      return db.selectFrom(USER)
          .where(USER.ID.eq(id))
          .fetchOptional()
          .map(it -> new User(
              it.getId(),
              it.getName(),
              it.getPasswordHash(),
              it.getEnabled(),
              authorities
          ));
    });
  }

  public void save(User user) {
    databaseStorage.acquireWriteTransaction(db -> {
      UserRecord userRecord = new UserRecord(
          user.getId(), user.isEnabled(), user.getName(), user.getPasswordHash()
      );

      if (existsUserById(user.getId())) {
        db.update(USER).set(userRecord).where(USER.ID.eq(user.getId())).execute();
      } else {
        db.insertInto(USER).set(userRecord).execute();
      }
    });
  }

  /**
   * Deletes a user by its id.
   *
   * @param id the id of the user
   * @return the affected row count
   */
  public int deleteUserById(String id) {
    return databaseStorage.acquireWriteTransaction(db -> {
      db.deleteFrom(USER_AUTHORITIES).where(USER_AUTHORITIES.USER_ID.eq(id)).execute();
      return db.deleteFrom(USER).where(USER.ID.eq(id)).execute();
    });
  }
}
