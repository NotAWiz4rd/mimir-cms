package de.seprojekt.se2019.g4.mimir.content;

import org.springframework.http.MediaType;

/**
 * This interface helps to work with collections with a mixed content of artifacts and folders.
 * Although artifacts and folders are different objects, they both shares similarities like id, parentUrl etc.
 */
public interface Content {

    Long getId();

    String getParentUrl();

    void setParentUrl(String parentUrl);

    String getTotalUrl();

    void setTotalUrl(String totalUrl);

    String getDisplayName();

    void setDisplayName(String displayName);

    boolean isLocked();

    String getLockedByName();

    MediaType getContentType();

    boolean isArtifact();
}
