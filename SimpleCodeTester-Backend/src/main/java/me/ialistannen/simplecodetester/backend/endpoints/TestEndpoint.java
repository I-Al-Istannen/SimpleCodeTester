package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.repos.CheckRepository;
import me.ialistannen.simplecodetester.backend.db.repos.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEndpoint {

  private CheckRepository checkRepository;
  private UserRepository userRepository;

  public TestEndpoint(CheckRepository checkRepository, UserRepository userRepository) {
    this.checkRepository = checkRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/test")
  public String loginStatus() {
    return SecurityContextHolder.getContext().getAuthentication().toString();
  }

  @PostMapping("/add")
  public String addCheck(@RequestBody @NotEmpty String check) {
    checkRepository.save(new CodeCheck(
        check, userRepository.findById("123").get()
    ));

    return "hey";
  }

  @GetMapping("/getCheck")
  public List<CodeCheck> checkChecks(@RequestParam @NotEmpty String studentId) {
    return checkRepository.findAllByCreatorId(studentId);
  }
}
