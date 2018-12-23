package me.ialistannen.simplecodetester.backend.db.repos;

import java.util.Optional;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

  /**
   * Finds a student by their student id.
   *
   * @param studentId the student id
   * @return the found student
   */
  Optional<User> findById(String studentId);
}
