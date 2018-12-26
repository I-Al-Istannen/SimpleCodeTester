package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckManageEndpoint {

  @GetMapping("/checks/get-all")
  public List<CodeCheck> getAll() {
    return Collections.emptyList();
  }
}
