package me.ialistannen.simplecodetester.backend.db.entities;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class User {

  private String id;
  private String name;
  @Expose(serialize = false)
  private String passwordHash;
  private boolean enabled;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> authorities;

  public User(String id, String name, String passwordHash, boolean enabled,
      List<String> authorities) {
    this.id = id;
    this.name = name;
    this.passwordHash = passwordHash;
    this.enabled = enabled;
    this.authorities = new ArrayList<>(authorities);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
