package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import javax.persistence.*;

/**
 * This class defines how the table thumbnail should look like (which columns, which primary/foreign keys etc.)
 * The result of a thumbnail table query will be mapped on objects from this class.
 */
@Entity
public class Thumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    private byte[] content;

    @Column
    private Long contentLength;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
