package de.seprojekt.se2019.g4.mimir.content.folder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.Objects;

/**
 * This class defines how the table folder should look like (which columns, which primary/foreign keys etc.)
 * The result of a folder table query will be mapped on objects from this class.
 */
@Entity

public class Folder {

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return id.equals(folder.id) &&
                Objects.equals(parentFolder, folder.parentFolder) &&
                name.equals(folder.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentFolder, name);
    }

    @Override
    public String toString() {
        return "Folder{" +
                "id=" + id +
                ", parentFolder=" + parentFolder +
                ", name='" + name +
                '}';
    }
}
