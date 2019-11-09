package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.Content;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;
import org.springframework.http.MediaType;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * This class defines how the table artifact should look like (which columns, which primary/foreign keys etc.)
 * The result of a artifact table query will be mapped on objects from this class.
 */
@Entity
public class Artifact implements Content {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JoinColumn
    @ManyToOne
    private Folder parentFolder;

    @Column(length = 512)
    private String displayName;

    @Column
    private String author;

    @Column
    private Instant lastChange;

    @ContentId
    @Column
    private UUID contentId;

    @ContentLength
    @Column
    private Long contentLength;

    @Column(length = 512)
    private MediaType contentType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "thumbnail_id")
    private Thumbnail thumbnail = new Thumbnail();

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getLastChange() {
        return lastChange;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean isArtifact() {
        return true;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return id.equals(artifact.id) &&
                parentFolder.equals(artifact.parentFolder) &&
                displayName.equals(artifact.displayName) &&
                author.equals(artifact.author) &&
                lastChange.equals(artifact.lastChange) &&
                contentId.equals(artifact.contentId) &&
                contentLength.equals(artifact.contentLength) &&
                contentType.equals(artifact.contentType) &&
                thumbnail.equals(artifact.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentFolder, displayName, author, lastChange, contentId, contentLength, contentType, thumbnail);
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id=" + id +
                ", parentFolder=" + parentFolder +
                ", displayName='" + displayName + '\'' +
                ", author='" + author + '\'' +
                ", lastChange=" + lastChange +
                ", contentId=" + contentId +
                ", contentLength=" + contentLength +
                ", contentType=" + contentType +
                ", thumbnail=" + thumbnail +
                '}';
    }
}
