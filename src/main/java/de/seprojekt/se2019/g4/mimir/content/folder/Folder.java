package de.seprojekt.se2019.g4.mimir.content.folder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.seprojekt.se2019.g4.mimir.content.Content;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.Objects;

/**
 * This class defines how the table folder should look like (which columns, which primary/foreign keys etc.)
 * The result of a folder table query will be mapped on objects from this class.
 */
@Entity
public class Folder implements Content {

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
    private String displayName;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public MediaType getContentType() {
        return MediaType.valueOf("inode/directory");
    }

    @Override
    public boolean isArtifact() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return id.equals(folder.id) &&
                Objects.equals(parentFolder, folder.parentFolder) &&
                displayName.equals(folder.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentFolder, displayName);
    }

    @Override
    public String toString() {
        return "Folder{" +
                "id=" + id +
                ", parentFolder=" + parentFolder +
                ", displayName='" + displayName +
                '}';
    }
}
