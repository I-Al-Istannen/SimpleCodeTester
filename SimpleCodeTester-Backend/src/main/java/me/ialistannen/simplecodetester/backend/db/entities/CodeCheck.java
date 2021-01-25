package me.ialistannen.simplecodetester.backend.db.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
  @JsonSerialize(using = UserSerializer.class)
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

  protected CodeCheck() {
  }

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

  private static class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(value.getName());
    }
  }
}
