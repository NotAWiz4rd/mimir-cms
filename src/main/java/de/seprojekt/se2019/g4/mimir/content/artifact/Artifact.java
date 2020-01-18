package de.seprojekt.se2019.g4.mimir.content.artifact;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.seprojekt.se2019.g4.mimir.content.comment.Comment;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.springframework.http.MediaType;

/**
 * This class defines how the table artifact should look like (which columns, which primary/foreign
 * keys etc.) The result of a artifact table query will be mapped on objects from this class.
 */
@Entity
public class Artifact {

  public static final String TYPE_IDENTIFIER = "A";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
  @JsonIdentityReference(alwaysAsId = true)
  @JsonProperty("parentId")
  @JoinColumn
  @ManyToOne
  private Folder parentFolder;

  @Column(length = 512)
  private String name;

  @Column
  @JsonProperty("author")
  private String author;

  @Column
  private Instant creationDate;

  @Column
  private Long contentLength;

  @JsonIgnore
  @Lob
  @Basic(fetch = FetchType.LAZY)
  private byte[] content;

  @Column(length = 512)
  private MediaType contentType;

  @JsonIgnore
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "thumbnail_id")
  private Thumbnail thumbnail = new Thumbnail();

  @JsonIgnore
  @JoinColumn
  @ManyToOne
  private Space space;

  @OneToMany(mappedBy = "artifact")
  @JsonIgnore
  private List<Comment> comments;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Folder getParentFolder() {
    return parentFolder;
  }

  public void setParentFolder(Folder parentFolder) {
    this.parentFolder = parentFolder;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Instant creationDate) {
    this.creationDate = creationDate;
  }

  public Long getContentLength() {
    return contentLength;
  }

  public void setContentLength(Long contentLength) {
    this.contentLength = contentLength;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public MediaType getContentType() {
    return contentType;
  }

  public void setContentType(MediaType contentType) {
    this.contentType = contentType;
  }

  public Thumbnail getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Thumbnail thumbnail) {
    this.thumbnail = thumbnail;
  }

  public Space getSpace() {
    return space;
  }

  public void setSpace(Space space) {
    this.space = space;
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Artifact artifact = (Artifact) o;
    return id.equals(artifact.id) &&
        parentFolder.equals(artifact.parentFolder) &&
        name.equals(artifact.name) &&
        creationDate.equals(artifact.creationDate) &&
        contentLength.equals(artifact.contentLength) &&
        Arrays.equals(content, artifact.content) &&
        contentType.equals(artifact.contentType) &&
        thumbnail.equals(artifact.thumbnail) &&
        author.equals(artifact.author) &&
        Objects.equals(space, artifact.space);
  }

  @Override
  public int hashCode() {
    int result = Objects
        .hash(id, parentFolder, name, creationDate, contentLength, contentType,
            thumbnail, author,
            space);
    result = 31 * result + Arrays.hashCode(content);
    return result;
  }

  @Override
  public String toString() {
    return "Artifact{" +
        "id=" + id +
        ", parentFolder=" + parentFolder +
        ", name='" + name + '\'' +
        ", author=" + author +
        ", creationDate=" + creationDate +
        ", contentLength=" + contentLength +
        ", content=" + Arrays.toString(content) +
        ", contentType=" + contentType +
        ", thumbnail=" + thumbnail +
        ", space=" + space +
        '}';
  }
}
