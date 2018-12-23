package me.ialistannen.simplecodetester.backend.endpoints;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEndpoint {

  @GetMapping("/test")
  public String loginStatus() {
    return SecurityContextHolder.getContext().getAuthentication().toString();
  }
}
