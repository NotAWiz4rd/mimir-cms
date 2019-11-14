package de.seprojekt.se2019.g4.mimir.content.space;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 512)
    private String name;

    @Column
    private String owner;

    @JoinColumn
    @ManyToOne
    @JsonIgnore
    private Folder rootFolder;

    @Override
    public String toString() {
        return "Space{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", rootFolder=" + rootFolder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Space space = (Space) o;
        return id.equals(space.id) &&
                name.equals(space.name) &&
                owner.equals(space.owner) &&
                rootFolder.equals(space.rootFolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, owner, rootFolder);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(Folder rootFolder) {
        this.rootFolder = rootFolder;
    }
}
