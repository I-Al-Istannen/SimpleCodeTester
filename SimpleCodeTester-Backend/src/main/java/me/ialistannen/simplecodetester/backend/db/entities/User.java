package me.ialistannen.simplecodetester.backend.db.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class User {

  private String id;
  private String name;
  @JsonIgnore
  private String passwordHash;
  private boolean enabled = true;

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
