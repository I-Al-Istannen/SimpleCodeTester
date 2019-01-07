package me.ialistannen.simplecodetester.backend.db.repos;

import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import org.springframework.data.repository.CrudRepository;

public interface CheckCategoryRepository extends CrudRepository<CheckCategory, Long> {

}
