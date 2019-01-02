package me.ialistannen.simplecodetester.backend.services.checks;

import com.google.gson.Gson;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.repos.CheckRepository;
import me.ialistannen.simplecodetester.backend.exception.InvalidCheckException;
import me.ialistannen.simplecodetester.backend.services.compilation.LocalCompilationService;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckType;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.ClassParsingUtil;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import org.joor.Reflect;
import org.springframework.stereotype.Service;

@Service
public class CodeCheckService {

  private CheckRepository checkRepository;
  private LocalCompilationService localCompilationService;
  private Gson gson;

  public CodeCheckService(CheckRepository checkRepository,
      LocalCompilationService localCompilationService) {
    this.checkRepository = checkRepository;
    this.localCompilationService = localCompilationService;
    this.gson = ConfiguredGson.createGson();
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
    validateCheckAndSetName(codeCheck);

    return checkRepository.save(codeCheck);
  }

  /**
   * Saves a {@link CodeCheck} in the repository that wants a given input and output.
   *
   * @param input the input to give
   * @param output the output to expect
   * @param name the name of the check
   * @param creator the {@link User} that created it
   * @return the added check, with its id field populated
   */
  public CodeCheck addIOCheck(List<String> input, String output, String name, User creator) {
    StaticInputOutputCheck outputCheck = new StaticInputOutputCheck(input, output, name);
    String payload = gson.toJson(outputCheck);

    CodeCheck codeCheck = new CodeCheck(payload, CheckType.IO, creator);
    codeCheck.setName(name);
    codeCheck.setApproved(true);
    return checkRepository.save(codeCheck);
  }

  /**
   * Saves a {@link CodeCheck} in the repository that wants a given input and output.
   *
   * @param id the id of the check
   * @param input the input to give
   * @param output the output to expect
   * @param name the name of the check
   * @return the added check, with its id field populated
   * @throws IllegalArgumentException if the check was no IO check
   */
  public boolean updateIOCheck(long id, List<String> input, String output, String name) {
    Optional<CodeCheck> check = getCheck(id);
    if (check.isEmpty()) {
      return false;
    }
    if (check.get().getCheckType() != CheckType.IO) {
      throw new IllegalArgumentException("Check is no IO check!");
    }

    StaticInputOutputCheck outputCheck = new StaticInputOutputCheck(input, output, name);
    String payload = gson.toJson(outputCheck);

    return updateCheck(id, codeCheck -> {
      codeCheck.setText(payload);
      codeCheck.setName(name);
    });
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
   * Validates a {@link CodeCheck}.
   *
   * @param codeCheck the code check to validate
   * @throws InvalidCheckException if the check is not valid
   */
  private void validateCheckAndSetName(CodeCheck codeCheck) {
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

    String name = Reflect.on(
        (Object) Reflect.on(file.asClass()).create().get()
    ).call("name").get();
    codeCheck.setName(name);
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

    validateCheckAndSetName(codeCheck.get());

    if (codeCheck.get().getCheckType() == CheckType.SOURCE_CODE) {
      codeCheck.get().setApproved(false);
    }

    checkRepository.save(codeCheck.get());

    return true;
  }

  private String removePackageDeclaration(String input) {
    return input.replaceAll("package.+;", "");
  }
}
