package me.ialistannen.simplecodetester.backend.services.checks;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.repos.CheckRepository;
import org.springframework.stereotype.Service;

@Service
public class CodeCheckService {

  private CheckRepository checkRepository;

  public CodeCheckService(CheckRepository checkRepository) {
    this.checkRepository = checkRepository;
  }

  /**
   * Returns all {@link CodeCheck}s.
   *
   * @return all code checks
   */
  public List<CodeCheck> getAll() {
    return StreamSupport.stream(checkRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  /**
   * Finds a check by its unique id.
   *
   * @param id the id of the check
   * @return the check, if found
   */
  public Optional<CodeCheck> getCheck(long id) {
    return checkRepository.findById(id);
  }


  /**
   * Finds all checks created by a given creator.
   *
   * @param ownerId the id of the check owner
   * @return all checks from that user
   */
  public List<CodeCheck> getChecksForOwner(String ownerId) {
    return checkRepository.findAllByCreatorId(ownerId);
  }

  /**
   * Saves a {@link CodeCheck} in the repository.
   *
   * @param codeCheck the check to save
   * @return the added check, with its id field populated
   */
  public CodeCheck addCheck(CodeCheck codeCheck) {
    return checkRepository.save(codeCheck);
  }

  /**
   * Deletes a {@link CodeCheck} from the repository.
   *
   * @param id the id of the check to remove
   * @return true if the check existed
   */
  @Transactional
  public boolean removeCheck(long id) {
    return checkRepository.deleteCodeCheckById(id) != 0;
  }

  /**
   * Deletes all checks.
   */
  public void removeAll() {
    checkRepository.deleteAll();
  }

  /**
   * Updates a {@link CodeCheck} in the repository.
   *
   * @param id the id of the check to update
   * @param updateAction the update action
   * @return true if the check existed
   */
  @Transactional
  public boolean updateCheck(long id, Consumer<CodeCheck> updateAction) {
    Optional<CodeCheck> codeCheck = checkRepository.findById(id);

    if (codeCheck.isEmpty()) {
      return false;
    }

    updateAction.accept(codeCheck.get());
    checkRepository.save(codeCheck.get());

    return true;
  }
}
