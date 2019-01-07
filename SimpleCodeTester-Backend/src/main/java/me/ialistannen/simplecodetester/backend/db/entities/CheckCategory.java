package me.ialistannen.simplecodetester.backend.db.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CheckCategory {

  @Id
  @GeneratedValue
  @Setter(AccessLevel.NONE)
  @EqualsAndHashCode.Include
  private long id;

  @NonNull
  private String name;

  /**
   * Is an entity.
   */
  protected CheckCategory() {
  }
}
