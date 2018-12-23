package me.ialistannen.simplecodetester.backend.db.repos;

import java.util.List;
import me.ialistannen.simplecodetester.backend.db.entities.Checks;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface CheckRepository extends CrudRepository<Checks, Long> {

  List<Checks> findAllByCreator(User creator);

  List<Checks> findAllByCreatorId(Long creatorId);
}
