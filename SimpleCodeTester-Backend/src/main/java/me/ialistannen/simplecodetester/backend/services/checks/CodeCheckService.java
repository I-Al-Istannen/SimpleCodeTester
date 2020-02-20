package me.ialistannen.simplecodetester.backend.services.checks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.repos.CheckRepository;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import org.springframework.stereotype.Service;

@Service
public class CodeCheckService {

  private CheckRepository checkRepository;
  private CheckSerializer checkSerializer;

  public CodeCheckService(CheckRepository checkRepository) {
    this.checkRepository = checkRepository;
    this.checkSerializer = new CheckSerializer(ConfiguredGson.createGson());
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
   * Returns the number of checks.
   *
   * @return the number of checks
   */
  public long getCount() {
    return checkRepository.count();
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
   * Checks if a check exists.
   *
   * @param id the id of the check
   * @return true if the check was found
   */
  public boolean containsCheck(long id) {
    return checkRepository.existsById(id);
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
   * @param check the check to save
   * @param creator the user who created it
   * @param category the check category
   * @return the added check, with its id field populated
   */
  public CodeCheck addCheck(Check check, User creator, CheckCategory category) {
    String json = checkSerializer.toJson(check);
    CodeCheck codeCheck = new CodeCheck(json, creator, category);

    codeCheck.setApproved(!check.needsApproval());
    codeCheck.setName(check.name());

    return checkRepository.save(codeCheck);
  }

  /**
   * Saves a {@link CodeCheck} in the repository that wants a given input and output.
   *
   * @param id the id of the check
   * @param newCheck the new check
   * @return the added check, with its id field populated
   * @throws IllegalArgumentException if the check was no IO check
   */
  @Transactional
  public boolean updateCheck(long id, Check newCheck) {
    Optional<CodeCheck> check = getCheck(id);
    if (check.isEmpty()) {
      return false;
    }

    String payload = checkSerializer.toJson(newCheck);

    CodeCheck codeCheck = check.get();
    codeCheck.setText(payload);
    codeCheck.setName(newCheck.name());
    codeCheck.setApproved(!newCheck.needsApproval());
    checkRepository.save(codeCheck);

    return true;
  }

  /**
   * Saves a {@link CodeCheck} in the repository.
   *
   * @param id the id of the check
   * @param approved whether the check is approved
   * @return false if the check could not be found
   */
  public boolean approveCheck(long id, boolean approved) {
    Optional<CodeCheck> check = getCheck(id);
    if (check.isEmpty()) {
      return false;
    }
    check.get().setApproved(approved);
    checkRepository.save(check.get()).isApproved();

    return true;
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
  void removeAll() {
    checkRepository.deleteAll();
  }
}
