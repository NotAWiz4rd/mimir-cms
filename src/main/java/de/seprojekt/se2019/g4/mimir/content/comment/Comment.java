package de.seprojekt.se2019.g4.mimir.content.comment;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
  @JsonIdentityReference(alwaysAsId = true)
  @JsonProperty("artifactId")
  @JoinColumn
  @ManyToOne
  private Artifact artifact;

  @Column
  @Lob
  private String text;

  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
  @JsonIdentityReference(alwaysAsId = true)
  @JsonProperty("authorId")
  @JoinColumn
  @ManyToOne
  private User author;

  @Column
  private Instant creationDate;

  public Comment() {
  }

  public Comment(Artifact artifact, String text, User author, Instant creationDate) {
    this.artifact = artifact;
    this.text = text;
    this.author = author;
    this.creationDate = creationDate;
  }

  @JsonProperty("author")
  public String getAuthorName() {
    return this.getAuthor().getName();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Artifact getArtifact() {
    return artifact;
  }

  public void setArtifact(Artifact artifact) {
    this.artifact = artifact;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Instant creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public boolean equals(Object o) {
    return Objects.equals(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, artifact, text, author, creationDate);
  }

  @Override
  public String toString() {
    return "Comment{" +
        "id=" + id +
        ", artifact=" + artifact +
        ", text='" + text + '\'' +
        ", author='" + author + '\'' +
        ", creationDate=" + creationDate +
        '}';
  }

}
