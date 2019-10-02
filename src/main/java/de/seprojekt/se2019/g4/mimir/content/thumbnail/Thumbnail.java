package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

/**
 * This class defines how the table thumbnail should look like (which columns, which primary/foreign keys etc.)
 * The result of a thumbnail table query will be mapped on objects from this class.
 */
@Entity
public class Thumbnail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ContentId
    private UUID contentId;

    @ContentLength
    private Long contentLength;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
