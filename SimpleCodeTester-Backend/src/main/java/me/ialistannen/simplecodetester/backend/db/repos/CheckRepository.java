package me.ialistannen.simplecodetester.backend.db.repos;

import java.util.List;
import javax.transaction.Transactional;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface CheckRepository extends CrudRepository<CodeCheck, Long> {

  /**
   * Finds all checks for a given creator's id.
   *
   * @param creatorId the {@link User#getId()} of the creator
   * @return all checks from that user
   */
  List<CodeCheck> findAllByCreatorId(String creatorId);

  /**
   * Deletes the check with the given id.
   *
   * @param id the id of the check
   * @return the amount of modified rows
   */
  @Transactional
  int deleteCodeCheckById(long id);

  /**
   * Checks if a given check exists.
   *
   * @param id the id of the check
   * @return true if the check existed
   */
  boolean existsById(long id);
}
