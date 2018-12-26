package me.ialistannen.simplecodetester.backend.services.user;

import java.util.Optional;
import java.util.function.Consumer;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.repos.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Adds a new user.
   *
   * @param user the user to add
   */
  public void addUser(User user) {
    if (containsUser(user.getId())) {
      throw new IllegalArgumentException("User did already exist!");
    }
    userRepository.save(user);
  }

  /**
   * Deletes a given user.
   *
   * @param userId the id of the user to delete
   * @return true if the user was deleted, false if it did not exist
   */
  public boolean deleteUser(String userId) {
    return userRepository.deleteUserById(userId) != 0;
  }

  /**
   * Checks if a given user already exists.
   *
   * @param userId the id of the user to check for
   * @return true if the user already exists
   */
  public boolean containsUser(String userId) {
    return userRepository.existsUserById(userId);
  }

  /**
   * Finds a given user.
   *
   * @param userId the id of the user to delete
   * @return the found user, if any
   */
  public Optional<User> getUser(String userId) {
    return userRepository.findById(userId);
  }

  /**
   * Updates a single user.
   *
   * @param userId the id of the user to update
   * @param update the update method
   * @return true if the user existed
   */
  public boolean updateUser(String userId, Consumer<User> update) {
    if (!userRepository.existsUserById(userId)) {
      return false;
    }
    User user = userRepository.getOne(userId);
    update.accept(user);
    userRepository.save(user);

    return true;
  }

  /**
   * Returns all users.
   *
   * @return all users
   */
  public Iterable<User> getAllUsers() {
    return userRepository.findAll();
  }
}
