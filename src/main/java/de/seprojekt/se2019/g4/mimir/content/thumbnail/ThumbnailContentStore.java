package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import org.springframework.content.commons.repository.ContentStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * This interface will be implemented by Spring Content FS.
 * The implementation will help us to store the associated thumbnail image file of {@link Thumbnail}
 * https://paulcwarren.github.io/spring-content/refs/snapshot/fs-index.html
 */
@Repository
public interface ThumbnailContentStore extends ContentStore<Thumbnail, UUID> {
}
