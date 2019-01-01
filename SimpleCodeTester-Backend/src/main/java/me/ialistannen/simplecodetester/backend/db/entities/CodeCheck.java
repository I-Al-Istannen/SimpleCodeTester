package me.ialistannen.simplecodetester.backend.db.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@ToString
@Getter
@Setter
@Entity
public class CodeCheck {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  @Setter(AccessLevel.NONE)
  private Long id;

  @Lob
  @NotEmpty
  private String text;

  @JsonSerialize(using = UserSerializer.class)
  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(referencedColumnName = "id")
  private User creator;

  /**
   * Whether the check is approved and allowed to run.
   */
  private boolean approved;

  @NotEmpty
  private String name;

  protected CodeCheck() {
  }

  /**
   * Creates a new CodeCheck.
   *
   * <p><br><strong>Remember to call {@link #setName(String)} before saving this
   * entity.</strong></p>
   *
   * @param text the text
   * @param creator the creator
   */
  public CodeCheck(@NotEmpty String text, User creator) {
    this.text = text;
    this.creator = creator;
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
