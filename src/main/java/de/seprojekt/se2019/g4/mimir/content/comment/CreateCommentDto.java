package de.seprojekt.se2019.g4.mimir.content.comment;

import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * This class is a template for data transfer objects for comment creation
 */
public class CreateCommentDto {

  @NotNull
  private Long artifactId;

  @NotNull
  private String text;

  public Long getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(Long artifactId) {
    this.artifactId = artifactId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public boolean equals(Object o) {
    return Objects.equals(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactId, text);
  }

  @Override
  public String toString() {
    return "CreateCommentDto{" +
        ", artifactId=" + artifactId +
        ", text='" + text + '\'' +
        '}';
  }

}
