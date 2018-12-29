package me.ialistannen.simplecodetester.backend.endpoints;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.security.Principal;
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
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckAlreadyRunningException;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.backend.exception.CompilationFailedException;
import me.ialistannen.simplecodetester.backend.services.checkrunning.CheckRunnerService;
import me.ialistannen.simplecodetester.backend.services.checks.CodeCheckService;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ClassParsingUtil;
import me.ialistannen.simplecodetester.util.StringOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Slf4j
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

    return test(id, fileToSubmission(source));
  }

  private ResponseEntity<Object> test(String id, Submission submission) {
    List<String> checks = codeCheckService.getAll().stream()
        .map(CodeCheck::getText)
        .collect(toList());

    try {
      return ResponseEntity.ok(
          checkRunnerService.check(id, submission, checks)
      );
    } catch (CompilationFailedException e) {
      return ResponseEntity.badRequest().body(e.getOutput());
    } catch (CheckRunningFailedException | CheckAlreadyRunningException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Test an uploaded zip file.
   *
   * @param file the uploaded zip file
   * @param user the user that requested it
   * @return the test result
   */
  @PostMapping("/test/zip")
  public ResponseEntity<Object> testZipFile(@RequestBody MultipartFile file,
      @NotNull Principal user) {
    Map<String, String> files = new HashMap<>();

    if (file == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "No file given!"));
    }

    try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        if (!entry.getName().endsWith(".java")) {
          return ResponseEntity.badRequest().body(Map.of(
              "error", String.format("File '%s' does not end with '.java'", entry.getName())
          ));
        }

        StringOutputStream stringOutputStream = new StringOutputStream();
        byte[] buffer = new byte[1024 * 6];
        int readLength;
        while ((readLength = zipInputStream.read(buffer)) >= 0) {
          stringOutputStream.write(buffer, 0, readLength);
        }

        files.put(entry.getName(), stringOutputStream.toString());
      }

      return testMultipleFiles(files, user.getName());
    } catch (IOException e) {
      log.info("Error extracting file for user '{}'", user.getName(), e);
      return ResponseEntity.badRequest()
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/test/multiple")
  public ResponseEntity<Object> testMultipleFiles(@NotNull Principal user,
      HttpServletRequest request) {

    if (!(request instanceof MultipartHttpServletRequest)) {
      return ResponseEntity.badRequest().body(Map.of("error", "no multipart request"));
    }

    MultiValueMap<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request)
        .getMultiFileMap();

    if (fileMap.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded!"));
    }

    try {
      Map<String, String> files = new HashMap<>();

      for (Entry<String, List<MultipartFile>> entry : fileMap.entrySet()) {
        for (MultipartFile file : entry.getValue()) {
          String source = new String(file.getBytes());
          String packageName = ClassParsingUtil.getPackage(source).map(s -> s + ".")
              .orElse("");

          files.put(packageName + file.getName(), source);
        }
      }

      return testMultipleFiles(files, user.getName());
    } catch (IOException e) {
      log.warn("Error fetching files", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error fetching files."));
    }
  }

  /**
   * Tests multiple files.
   *
   * @param files a map from file name to file content
   * @param userId the id of the user
   * @return the resulting response
   */
  private ResponseEntity<Object> testMultipleFiles(Map<String, String> files, String userId) {
    if (files.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "No files given."));
    }

    Submission submission = ImmutableSubmission.builder()
        .putAllFiles(files)
        .build();

    return test(userId, submission);
  }

  private Submission fileToSubmission(String source) {
    String className = ClassParsingUtil.getClassName(source)
        .orElseThrow(() -> new RuntimeException("No class declaration found"));

    return ImmutableSubmission.builder()
        .putFiles(className + ".java", source)
        .build();
  }
}
