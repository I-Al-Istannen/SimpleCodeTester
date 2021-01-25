package me.ialistannen.simplecodetester.backend.db.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CheckCategory {

  @Setter(AccessLevel.NONE)
  @EqualsAndHashCode.Include
  private int id;
  @NonNull
  private String name;
}
