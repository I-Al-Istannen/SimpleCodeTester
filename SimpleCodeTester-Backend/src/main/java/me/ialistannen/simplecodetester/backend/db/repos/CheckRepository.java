package me.ialistannen.simplecodetester.backend.db.repos;

import java.util.List;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface CheckRepository extends CrudRepository<CodeCheck, Long> {

  List<CodeCheck> findAllByCreator(User creator);

  List<CodeCheck> findAllByCreatorId(String creatorId);
}
