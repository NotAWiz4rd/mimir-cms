package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.Content;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

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

    @Column(length = 512)
    private String parentUrl;

    @Column(unique = true, length = 512)
    private String totalUrl;

    @Column(length = 512)
    private String displayName;

    @Column
    private String author;

    @Column
    private boolean locked;

    @Column
    private String lockedByName;

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
        Assert.isTrue(!totalUrl.endsWith("/"), "totalUrl darf nicht mit einem Schrägstrich enden");
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

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    @Override
    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public boolean isArtifact() {
        return true;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String getLockedByName() {
        return lockedByName;
    }

    public void setLockedByName(String lockedByName) {
        this.lockedByName = lockedByName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return locked == artifact.locked &&
                Objects.equals(id, artifact.id) &&
                Objects.equals(parentUrl, artifact.parentUrl) &&
                Objects.equals(totalUrl, artifact.totalUrl) &&
                Objects.equals(displayName, artifact.displayName) &&
                Objects.equals(lockedByName, artifact.lockedByName) &&
                Objects.equals(contentId, artifact.contentId) &&
                Objects.equals(contentLength, artifact.contentLength) &&
                Objects.equals(contentType, artifact.contentType) &&
                Objects.equals(thumbnail, artifact.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentUrl, totalUrl, displayName, locked, lockedByName, contentId, contentLength, contentType, thumbnail);
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id=" + id +
                ", parentUrl='" + parentUrl + '\'' +
                ", totalUrl='" + totalUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                ", locked=" + locked +
                ", lockedByName='" + lockedByName + '\'' +
                ", contentId='" + contentId + '\'' +
                ", contentLength=" + contentLength +
                ", contentType=" + contentType +
                ", thumbnail=" + thumbnail +
                '}';
    }
}
