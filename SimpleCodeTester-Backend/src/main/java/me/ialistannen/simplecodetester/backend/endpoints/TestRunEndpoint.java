package me.ialistannen.simplecodetester.backend.endpoints;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.backend.services.checkrunning.CheckRunnerService;
import me.ialistannen.simplecodetester.backend.services.checks.CodeCheckService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.result.ImmutableResult;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ClassParsingUtil;
import me.ialistannen.simplecodetester.util.StringOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Slf4j
@RestController
public class TestRunEndpoint {

  // * 1024 -> KB
  // * 1024 -> MB
  // So allow 10 megabytes
  private static final int MAX_ZIP_CONTENT_SIZE = 1024 * 1024 * 10;

  private final CheckRunnerService checkRunnerService;
  private final CodeCheckService codeCheckService;
  private final Counter testsRunCounter;
  private final Counter submissionsTestedCounter;
  private final Counter failedCompilationsCounter;
  private final Counter executionErrorsCounter;
  private final Counter failedTestsCounter;

  public TestRunEndpoint(CheckRunnerService checkRunnerService, CodeCheckService codeCheckService) {
    this.checkRunnerService = checkRunnerService;
    this.codeCheckService = codeCheckService;
    testsRunCounter = Counter.builder("tests_total")
        .description("Total amount of executed tests")
        .register(Metrics.globalRegistry);
    submissionsTestedCounter = Counter.builder("submissions_tested")
        .description("Total amount of tested submissions")
        .register(Metrics.globalRegistry);
    failedCompilationsCounter = Counter.builder("failed_compilations")
        .description("Total amount of submissions that failed to compile")
        .register(Metrics.globalRegistry);
    executionErrorsCounter = Counter.builder("execution_errors")
        .description("Total amount of submissions that triggered execution errors")
        .register(Metrics.globalRegistry);
    failedTestsCounter = Counter.builder("failed_tests")
        .description("Total amount of failed tests")
        .register(Metrics.globalRegistry);
  }

  @PostMapping("/test/single/{categoryId}")
  public ResponseEntity<Object> testSingleFile(@PathVariable long categoryId,
      @RequestBody @NotEmpty String source, @NotNull
      Principal user) {
    String id = user.getName();

    if (ClassParsingUtil.getClassName(source).isEmpty()) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "No class declaration found.");
    }

    String className = ClassParsingUtil.getClassName(source).orElseThrow();
    Submission submission = ImmutableSubmission.builder()
        .putFiles(className + ".java", source)
        .build();

    return test(id, submission, categoryId);
  }

  private ResponseEntity<Object> test(String id, Submission submission, long categoryId) {
    List<CodeCheck> checks = codeCheckService.getAll().stream()
        .filter(CodeCheck::isApproved)
        .filter(codeCheck -> codeCheck.getCategory().getId() == categoryId)
        .collect(toList());

    if (checks.isEmpty()) {
      return ResponseUtil.error(HttpStatus.NOT_FOUND, "No checks I am allowed to run found.");
    }

    submissionsTestedCounter.increment();

    try {
      Result checkResult = checkRunnerService.check(id, submission, checks);

      if (checkResult.compilationOutput().isPresent()) {
        CompilationOutput output = checkResult.compilationOutput().get();
        if (!output.successful()) {
          failedCompilationsCounter.increment();
          return ResponseEntity.ok().body(output);
        }
      }

      Map<String, List<CheckResult>> skippedChecksRemoved = new HashMap<>();

      for (Entry<String, List<CheckResult>> entry : checkResult.fileResults().entrySet()) {
        List<CheckResult> withoutSkipped = entry.getValue().stream()
            .filter(result -> result.result() != ResultType.NOT_APPLICABLE)
            .collect(toList());

        if (withoutSkipped.isEmpty()) {
          continue;
        }

        skippedChecksRemoved.put(entry.getKey(), withoutSkipped);
      }

      testsRunCounter.increment(
          checkResult.fileResults().values().stream()
              .flatMap(Collection::stream)
              .filter(it -> it.result() != ResultType.NOT_APPLICABLE)
              .count()
      );
      long failedTestCount = checkResult.fileResults().values().stream()
          .flatMap(Collection::stream)
          .filter(it -> it.result() == ResultType.FAILED)
          .count();
      failedTestsCounter.increment(failedTestCount);

      return ResponseEntity.ok(
          ImmutableResult.builder()
              .from(checkResult)
              .fileResults(skippedChecksRemoved)
              .build()
      );
    } catch (CheckRunningFailedException e) {
      executionErrorsCounter.increment();
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Test an uploaded zip file.
   *
   * @param file the uploaded zip file
   * @param user the user that requested it
   * @return the test result
   */
  @PostMapping("/test/zip/{categoryId}")
  public ResponseEntity<Object> testZipFile(@PathVariable("categoryId") long categoryId,
      @RequestBody MultipartFile file,
      @NotNull Principal user) {
    Map<String, String> files = new HashMap<>();

    if (file == null) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "No file uploaded!");
    }

    long uncompressedSize = 0;

    try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        // Skip non-java files
        if (!entry.getName().endsWith(".java")) {
          continue;
        }
        // Skip hidden files. Java classes can never start with a "." so this is okay
        if (entry.getName().startsWith(".")) {
          continue;
        }
        // Skip hidden files. Java classes can never start with a "." so this is okay
        if (entry.getName().contains("/") && !entry.getName().endsWith("/")) {
          String fileName = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
          if (fileName.startsWith(".")) {
            continue;
          }
        }

        StringOutputStream stringOutputStream = new StringOutputStream();
        byte[] buffer = new byte[1024 * 6];
        int readLength;
        while ((readLength = zipInputStream.read(buffer)) >= 0) {
          uncompressedSize += readLength;

          if (uncompressedSize > MAX_ZIP_CONTENT_SIZE) {
            return ResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                "Zip file too large!"
            );
          }

          stringOutputStream.write(buffer, 0, readLength);
        }

        String source = stringOutputStream.toString();
        String packageName = ClassParsingUtil.getPackage(source)
            .map(s -> s.replace(".", "/"))
            .map(s -> s + "/")
            .orElse("");

        String fileName = Paths.get(entry.getName()).getFileName().toString();
        files.put(packageName + fileName, source);
      }

      return testMultipleFiles(files, user.getName(), categoryId);
    } catch (MalformedInputException | IllegalArgumentException e) {
      log.info("Error extracting file for user '{}'", user.getName(), e);
      return ResponseUtil.error(
          HttpStatus.BAD_REQUEST,
          "Malformed input? Make sure your file names are in UTF-8! Message is " + e.getMessage()
      );
    } catch (IOException e) {
      log.info("Error extracting file for user '{}'", user.getName(), e);
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PostMapping("/test/multiple/{categoryId}")
  public ResponseEntity<Object> testMultipleFiles(@PathVariable("categoryId") long categoryId,
      @NotNull Principal user,
      HttpServletRequest request) {

    if (!(request instanceof MultipartHttpServletRequest)) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "No multipart request");
    }

    MultiValueMap<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request)
        .getMultiFileMap();

    if (fileMap.isEmpty()) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "No file uploaded!");
    }

    try {
      Map<String, String> files = new HashMap<>();

      for (Entry<String, List<MultipartFile>> entry : fileMap.entrySet()) {
        for (MultipartFile file : entry.getValue()) {
          String source = new String(file.getBytes());
          String packageName = ClassParsingUtil.getPackage(source)
              .map(s -> s + "/")
              .orElse("");

          files.put(packageName + file.getName(), source);
        }
      }

      return testMultipleFiles(files, user.getName(), categoryId);
    } catch (IOException e) {
      log.warn("Error fetching files for " + user.getName(), e);
      return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching files");
    }
  }

  /**
   * Tests multiple files.
   *
   * @param files a map from file name to file content
   * @param userId the id of the user
   * @param categoryId the if the {@link CheckCategory} to use
   * @return the resulting response
   */
  private ResponseEntity<Object> testMultipleFiles(Map<String, String> files, String userId,
      long categoryId) {
    if (files.isEmpty()) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "No java files received!");
    }

    Submission submission = ImmutableSubmission.builder()
        .putAllFiles(files)
        .build();

    return test(userId, submission, categoryId);
  }

}
