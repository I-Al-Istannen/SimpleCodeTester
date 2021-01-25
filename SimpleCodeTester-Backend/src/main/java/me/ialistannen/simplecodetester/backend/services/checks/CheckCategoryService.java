package me.ialistannen.simplecodetester.backend.services.checks;

import java.util.List;
import java.util.Optional;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.repos.CheckCategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CheckCategoryService {

  private final CheckCategoryRepository repository;

  public CheckCategoryService(CheckCategoryRepository repository) {
    this.repository = repository;
  }

  /**
   * Returns all {@link CheckCategory CheckCategories}.
   *
   * @return all check categories
   */
  public List<CheckCategory> getAll() {
    return repository.findAll();
  }

  /**
   * Returns a {@link CheckCategory} by its id.
   *
   * @param id the id
   * @return the {@link CheckCategory} with that id
   */
  public Optional<CheckCategory> getById(int id) {
    return repository.findById(id);
  }

  /**
   * Adds a new {@link CheckCategory}.
   *
   * @param name the name of the category
   * @return the created category with its id field populated
   */
  public CheckCategory addCategory(String name) {
    return repository.save(name);
  }

  /**
   * Renames a category.
   *
   * @param id the id of the category
   * @param newName the new name for it
   * @return the renamed category
   */
  public Optional<CheckCategory> rename(int id, String newName) {
    return repository.rename(id, newName);
  }

  /**
   * Deletes a {@link CheckCategory}.
   *
   * @param id the if of the category
   * @return true if the category existed
   */
  public boolean removeCategory(int id) {
    return repository.deleteById(id) != 0;
  }
}
