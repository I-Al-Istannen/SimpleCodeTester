package me.ialistannen.simplecodetester.backend.services.checks;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.repos.CheckRepository;
import me.ialistannen.simplecodetester.backend.exception.InvalidCheckException;
import me.ialistannen.simplecodetester.backend.services.compilation.LocalCompilationService;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.ClassParsingUtil;
import org.springframework.stereotype.Service;

@Service
public class CodeCheckService {

  private CheckRepository checkRepository;
  private LocalCompilationService localCompilationService;

  public CodeCheckService(CheckRepository checkRepository,
      LocalCompilationService localCompilationService) {
    this.checkRepository = checkRepository;
    this.localCompilationService = localCompilationService;
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
   * @param codeCheck the check to save
   * @return the added check, with its id field populated
   * @throws InvalidCheckException if the check does not compile
   */
  public CodeCheck addCheck(CodeCheck codeCheck) {
    validateCheck(codeCheck);

    return checkRepository.save(codeCheck);
  }

  /**
   * Validates a {@link CodeCheck}.
   *
   * @param codeCheck the code check to validate
   * @throws InvalidCheckException if the check is not valid
   */
  private void validateCheck(CodeCheck codeCheck) {
    String body = removePackageDeclaration(codeCheck.getText()).trim();
    String className = ClassParsingUtil.getClassName(body)
        .orElseThrow(() -> new InvalidCheckException("No class declaration found."));

    codeCheck.setText(body);

    CompilationOutput compilationOutput = localCompilationService
        .compile(className + ".java", body);

    if (!compilationOutput.successful()) {
      throw new InvalidCheckException(compilationOutput);
    }

    if (compilationOutput.files().size() != 1) {
      throw new InvalidCheckException("Not exactly one output file!");
    }

    CompiledFile file = compilationOutput.files().iterator().next();

    if (!Check.class.isAssignableFrom(file.asClass())) {
      throw new InvalidCheckException(
          "Class '" + file.qualifiedName() + "' does not extend Check!"
      );
    }
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
   * @throws InvalidCheckException if the {@link CodeCheck} is invalid after the change
   */
  @Transactional
  public boolean updateCheck(long id, Consumer<CodeCheck> updateAction) {
    Optional<CodeCheck> codeCheck = checkRepository.findById(id);

    if (codeCheck.isEmpty()) {
      return false;
    }

    updateAction.accept(codeCheck.get());

    validateCheck(codeCheck.get());

    checkRepository.save(codeCheck.get());

    return true;
  }

  private String removePackageDeclaration(String input) {
    return input.replaceAll("package.+;", "");
  }
}
