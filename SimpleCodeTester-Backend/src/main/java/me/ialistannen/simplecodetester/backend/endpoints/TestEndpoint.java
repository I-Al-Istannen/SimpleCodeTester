package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotEmpty;
import me.ialistannen.simplecodetester.backend.db.entities.Checks;
import me.ialistannen.simplecodetester.backend.db.repos.CheckRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEndpoint {

  private CheckRepository checkRepository;

  public TestEndpoint(CheckRepository checkRepository) {
    this.checkRepository = checkRepository;
  }

  @GetMapping("/test")
  public String loginStatus() {
    return SecurityContextHolder.getContext().getAuthentication().toString();
  }

  @GetMapping("/getCheck")
  @PermitAll
  public List<Checks> checkChecks(@RequestParam @NotEmpty String studentId) {
    return checkRepository.findAllByCreatorId(Long.valueOf(studentId));
  }
}
