package de.seprojekt.se2019.g4.mimir.content.artifact;

import com.fasterxml.jackson.annotation.*;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import org.springframework.http.MediaType;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * This class defines how the table artifact should look like (which columns, which primary/foreign keys etc.)
 * The result of a artifact table query will be mapped on objects from this class.
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return id.equals(artifact.id) &&
                parentFolder.equals(artifact.parentFolder) &&
                name.equals(artifact.name) &&
                author.equals(artifact.author) &&
                creationDate.equals(artifact.creationDate) &&
                contentLength.equals(artifact.contentLength) &&
                contentType.equals(artifact.contentType) &&
                thumbnail.equals(artifact.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentFolder, name, author, creationDate, contentLength, contentType, thumbnail);
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id=" + id +
                ", parentFolder=" + parentFolder +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", creationDate=" + creationDate +
                ", contentLength=" + contentLength +
                ", contentType=" + contentType +
                ", thumbnail=" + thumbnail +
                '}';
    }


}
