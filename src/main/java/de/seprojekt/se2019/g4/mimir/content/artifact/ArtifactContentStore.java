package de.seprojekt.se2019.g4.mimir.content.artifact;


import org.springframework.content.commons.repository.ContentStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * This interface will be implemented by Spring Content FS.
 * The implementation will help us to store the associated artifact file of {@link Artifact}
 * https://paulcwarren.github.io/spring-content/refs/snapshot/fs-index.html
 */
@Repository
public interface ArtifactContentStore extends ContentStore<Artifact, UUID> {
}
