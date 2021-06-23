package me.ialistannen.simplecodetester.backend.db.entities;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.ialistannen.simplecodetester.checks.CheckType;

@ToString
@Getter
@Setter
@AllArgsConstructor
public class CodeCheck {

  private Integer id;
  private String text;
  @JsonAdapter(SerializeNameOnly.class)
  private User creator;
  private CheckCategory category;
  private Instant creationTime;
  private Instant updateTime;
  /**
   * Whether the check is approved and allowed to run.
   */
  private boolean approved;
  private CheckType checkType = CheckType.INTERLEAVED_IO;
  private String name;

  /**
   * Creates a new CodeCheck.
   *
   * @param text the text
   * @param creator the creator
   * @param category the {@link CheckCategory}
   * @param approved whether this check is approved
   * @param name the name of the check
   */
  public CodeCheck(@NotEmpty String text, User creator, CheckCategory category, String name,
      boolean approved) {
    this.text = text;
    this.creator = creator;
    this.category = category;
    this.creationTime = Instant.now();
    this.name = name;
    this.approved = approved;
  }

  public Optional<Instant> getUpdateTime() {
    return Optional.ofNullable(updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CodeCheck codeCheck = (CodeCheck) o;
    return Objects.equals(id, codeCheck.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  private static class SerializeNameOnly extends TypeAdapter<User> {

    @Override
    public void write(JsonWriter out, User user) throws IOException {
      out.value(user.getName());
    }

    @Override
    public User read(JsonReader in) {
      throw new IllegalStateException("You can not deserialize this field");
    }
  }
}
