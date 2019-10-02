package de.seprojekt.se2019.g4.mimir.content;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import org.springframework.http.MediaType;

public class ContentHelper {

    public static Artifact createArtifact(String parentUrl, String name) {
        org.springframework.util.Assert.isTrue(parentUrl.endsWith("/"), "parentUrl must end with forward slash");
        org.springframework.util.Assert.isTrue(!name.endsWith("/"), "name must not end with forward slash");

        Artifact artifact = new Artifact();
        artifact.setParentUrl(parentUrl);
        artifact.setTotalUrl(parentUrl + name);
        artifact.setDisplayName(name);
        artifact.setContentType(MediaType.TEXT_PLAIN);
        return artifact;
    }

    public static Folder createFolder(String parentUrl, String nameWithoutEndSlash) {
        org.springframework.util.Assert.isTrue(parentUrl.endsWith("/"), "parentUrl must end with forward slash");
        org.springframework.util.Assert.isTrue(!nameWithoutEndSlash.endsWith("/"), "nameWithoutEndSlash must not end with forward slash");

        Folder folder = new Folder();
        folder.setParentUrl(parentUrl);
        folder.setTotalUrl(parentUrl + nameWithoutEndSlash + "/");
        folder.setDisplayName(nameWithoutEndSlash);
        return folder;
    }
}
