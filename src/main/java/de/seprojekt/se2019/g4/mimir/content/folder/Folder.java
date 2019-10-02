package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.Content;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Objects;

/**
 * This class defines how the table folder should look like (which columns, which primary/foreign keys etc.)
 * The result of a folder table query will be mapped on objects from this class.
 */
@Entity
public class Folder implements Content {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(length = 512)
    private String parentUrl;

    @Column(unique = true, length = 512)
    private String totalUrl;

    @Column(length = 512)
    private String displayName;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getParentUrl() {
        return parentUrl;
    }

    @Override
    public void setParentUrl(String parentUrl) {
        Assert.isTrue(parentUrl.endsWith("/"), "parentUrl muss mit einem Schrägstrich enden");
        this.parentUrl = parentUrl;
    }

    @Override
    public String getTotalUrl() {
        return totalUrl;
    }

    @Override
    public void setTotalUrl(String totalUrl) {
        Assert.isTrue(totalUrl.endsWith("/"), "totalUrl muss mit einem Schrägstrich enden");
        this.totalUrl = totalUrl;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public String getLockedByName() {
        return "";
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
        return Objects.equals(id, folder.id) &&
                Objects.equals(parentUrl, folder.parentUrl) &&
                Objects.equals(totalUrl, folder.totalUrl) &&
                Objects.equals(displayName, folder.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentUrl, totalUrl, displayName);
    }

    @Override
    public String toString() {
        return "Folder{" +
                "id=" + id +
                ", parentUrl='" + parentUrl + '\'' +
                ", totalUrl='" + totalUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
