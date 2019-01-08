package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.services.checks.CheckCategoryService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CheckCategoryEndpoint {

  private CheckCategoryService checkCategoryService;

  public CheckCategoryEndpoint(CheckCategoryService checkCategoryService) {
    this.checkCategoryService = checkCategoryService;
  }

  @GetMapping("/check-category/get-all")
  public List<CheckCategory> getAll() {
    return checkCategoryService.getAll();
  }

  @GetMapping("/check-category/get")
  public ResponseEntity<Object> getForId(@RequestParam long id) {
    return ResponseEntity.ok(checkCategoryService.getById(id));
  }

  @DeleteMapping("/check-category/delete/{id}")
  @RolesAllowed("ROLE_ADMIN")
  public ResponseEntity<Object> delete(@PathVariable("id") long id) {
    if (checkCategoryService.removeCategory(id)) {
      return ResponseEntity.ok("{}");
    }
    return ResponseUtil.error(HttpStatus.NOT_FOUND, "Check not found!");
  }

  @PostMapping("/check-category/add-new")
  @RolesAllowed("ROLE_ADMIN")
  public ResponseEntity<CheckCategory> addNew(@RequestParam @NotEmpty String name) {
    return ResponseEntity.ok(checkCategoryService.addCategory(name));
  }
}
