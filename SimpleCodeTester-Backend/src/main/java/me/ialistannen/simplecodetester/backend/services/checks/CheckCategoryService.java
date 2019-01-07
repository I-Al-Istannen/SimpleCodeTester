package me.ialistannen.simplecodetester.backend.services.checks;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.repos.CheckCategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CheckCategoryService {

  private CheckCategoryRepository repository;

  public CheckCategoryService(CheckCategoryRepository repository) {
    this.repository = repository;
  }

  /**
   * Returns all {@link CheckCategory CheckCategories}.
   *
   * @return all check categories
   */
  public List<CheckCategory> getAll() {
    return StreamSupport.stream(repository.findAll().spliterator(), false)
        .collect(toList());
  }

  /**
   * Returns a {@link CheckCategory} by its id.
   *
   * @param id the id
   * @return the {@link CheckCategory} with that id
   */
  public Optional<CheckCategory> getById(long id) {
    return repository.findById(id);
  }

  /**
   * Adds a new {@link CheckCategory}.
   *
   * @param name the name of the category
   * @return the created category with its id field populated
   */
  public CheckCategory addCategory(String name) {
    return repository.save(new CheckCategory(name));
  }

  /**
   * Deletes a {@link CheckCategory}.
   *
   * @param id the if of the category
   * @return true if the category existed
   */
  public boolean removeCategory(long id) {
    if (!repository.existsById(id)) {
      return false;
    }
    repository.deleteById(id);

    return true;
  }
}
