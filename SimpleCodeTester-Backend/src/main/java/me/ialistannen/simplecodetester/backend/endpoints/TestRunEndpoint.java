package me.ialistannen.simplecodetester.backend.endpoints;

import static java.util.stream.Collectors.toList;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckAlreadyRunningException;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.backend.exception.CompilationFailedException;
import me.ialistannen.simplecodetester.backend.services.checkrunning.CheckRunnerService;
import me.ialistannen.simplecodetester.backend.services.checks.CodeCheckService;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ClassParsingUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRunEndpoint {

  private CheckRunnerService checkRunnerService;
  private CodeCheckService codeCheckService;

  public TestRunEndpoint(CheckRunnerService checkRunnerService, CodeCheckService codeCheckService) {
    this.checkRunnerService = checkRunnerService;
    this.codeCheckService = codeCheckService;
  }

  @PostMapping("/test/single")
  public ResponseEntity<Object> testSingleFile(@RequestBody @NotEmpty String source, @NotNull
      Principal user) {
    String id = user.getName();

    List<String> checks = codeCheckService.getAll().stream()
        .map(CodeCheck::getText)
        .collect(toList());

    try {
      return ResponseEntity.ok(
          checkRunnerService.check(id, fileToSubmission(source), checks)
      );
    } catch (CompilationFailedException e) {
      return ResponseEntity.badRequest().body(e.getOutput());
    } catch (CheckRunningFailedException | CheckAlreadyRunningException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  private Submission fileToSubmission(String source) {
    String className = ClassParsingUtil.getClassName(source)
        .orElseThrow(() -> new RuntimeException("No class declaration found"));

    return ImmutableSubmission.builder()
        .putFiles(className + ".java", source)
        .build();
  }
}
