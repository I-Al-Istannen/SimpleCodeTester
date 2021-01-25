package me.ialistannen.simplecodetester.backend.services.checks;

import java.time.Instant;
import java.util.ArrayList;
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

  private final CheckRepository checkRepository;
  private final CheckSerializer checkSerializer;

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
    return checkRepository.findAll();
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
  public Optional<CodeCheck> getCheck(int id) {
    return checkRepository.findById(id);
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
    CodeCheck codeCheck = new CodeCheck(
        json, creator, category, check.name(), !check.needsApproval()
    );

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
  public boolean updateCheck(int id, Check newCheck) {
    Optional<CodeCheck> check = getCheck(id);
    if (check.isEmpty()) {
      return false;
    }

    String payload = checkSerializer.toJson(newCheck);

    CodeCheck codeCheck = check.get();
    codeCheck.setText(payload);
    codeCheck.setName(newCheck.name());
    codeCheck.setApproved(!newCheck.needsApproval());
    codeCheck.setUpdateTime(Instant.now());
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
  public boolean approveCheck(int id, boolean approved) {
    Optional<CodeCheck> check = getCheck(id);
    if (check.isEmpty()) {
      return false;
    }
    check.get().setApproved(approved);
    checkRepository.save(check.get());

    return true;
  }

  /**
   * Deletes a {@link CodeCheck} from the repository.
   *
   * @param id the id of the check to remove
   * @return true if the check existed
   */
  @Transactional
  public boolean removeCheck(int id) {
    return checkRepository.deleteCodeCheckById(id) != 0;
  }

  /**
   * Deletes all checks.
   */
  void removeAll() {
    checkRepository.deleteAll();
  }
}
