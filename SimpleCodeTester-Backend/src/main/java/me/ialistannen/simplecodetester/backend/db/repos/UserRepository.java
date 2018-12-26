package me.ialistannen.simplecodetester.backend.db.repos;

import javax.transaction.Transactional;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  /**
   * Deletes a user by its id.
   *
   * @param id the id of the user
   * @return the affected row count
   */
  @Transactional
  int deleteUserById(String id);

  /**
   * Checks if a given user exists.
   *
   * @param id the id of the user
   * @return true if the user exists
   */
  boolean existsUserById(String id);
}
