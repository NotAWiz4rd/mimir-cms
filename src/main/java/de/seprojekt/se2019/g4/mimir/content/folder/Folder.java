package de.seprojekt.se2019.g4.mimir.content.folder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.springframework.lang.Nullable;

/**
 * This class defines how the table folder should look like (which columns, which primary/foreign
 * keys etc.) The result of a folder table query will be mapped on objects from this class.
 */
@Entity
public class Folder {

  public static final String TYPE_IDENTIFIER = "F";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
  @JsonIdentityReference(alwaysAsId = true)
  @JsonProperty("parentId")
  @Nullable
  @JoinColumn
  @ManyToOne
  private Folder parentFolder;

  @Column(length = 512)
  private String name;

  @JsonIgnore
  @JoinColumn
  @ManyToOne
  @Nullable
  private Space space;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Nullable
  public Folder getParentFolder() {
    return parentFolder;
  }

  public void setParentFolder(@Nullable Folder parentFolder) {
    this.parentFolder = parentFolder;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Space getSpace() {
    return space;
  }

  public void setSpace(Space space) {
    this.space = space;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Folder folder = (Folder) o;
    return id.equals(folder.id) &&
        parentFolder.equals(folder.parentFolder) &&
        name.equals(folder.name) &&
        Objects.equals(space, folder.space);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, parentFolder, name, space);
  }

  @Override
  public String toString() {
    return "Folder{" +
        "id=" + id +
        ", parentFolder=" + parentFolder +
        ", name='" + name + '\'' +
        '}';
  }
}
