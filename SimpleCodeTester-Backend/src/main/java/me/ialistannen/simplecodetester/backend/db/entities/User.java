package me.ialistannen.simplecodetester.backend.db.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Getter
@ToString
public class User {

  @Id
  @Column(name = "id", unique = true)
  private String id;
  private String name;
  private String passwordHash;
  @Column(name = "enabled")
  private Boolean enabled = true;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> authorities;

  protected User() {
  }

  public User(String id, String name, String passwordHash, Boolean enabled,
      List<String> authorities) {
    this.id = id;
    this.name = name;
    this.passwordHash = passwordHash;
    this.enabled = enabled;
    this.authorities = new ArrayList<>(authorities);
  }

  /**
   * Converts this user to a spring user.
   *
   * @return the spring user
   */
  public org.springframework.security.core.userdetails.User toSpringUser() {
    return new org.springframework.security.core.userdetails.User(
        id,
        passwordHash,
        enabled,
        true,
        true,
        true,
        getGrantedAuthorities()
    );
  }

  private List<GrantedAuthority> getGrantedAuthorities() {
    return authorities.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}
