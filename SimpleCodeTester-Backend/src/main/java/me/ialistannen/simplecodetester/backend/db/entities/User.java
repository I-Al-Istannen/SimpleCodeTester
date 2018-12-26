package me.ialistannen.simplecodetester.backend.db.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.ToString;

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

  public String getName() {
    return name;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public List<String> getAuthorities() {
    return authorities;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public void setAuthorities(List<String> authorities) {
    this.authorities = authorities;
  }
}
