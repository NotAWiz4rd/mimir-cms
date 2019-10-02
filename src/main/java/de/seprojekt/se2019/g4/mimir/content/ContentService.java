package de.seprojekt.se2019.g4.mimir.content;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactRepository;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class offers help methods for displaying artifacts and folders simultaneous.
 */
@Service
public class ContentService {
    private static final List<String> UNSAFE_STRINGS = List.of("%2e", "%2E", ";", "%3b", "%3B", "%2f", "%2F", "\\", "%5c", "%5C", "%25");
    private final static Logger LOGGER = LoggerFactory.getLogger(ContentService.class);

    private FolderRepository folderRepository;
    private ArtifactRepository artifactRepository;

    /**
     * The parameters will be autowired by Spring.
     * @param folderRepository
     * @param artifactRepository
     */
    public ContentService(FolderRepository folderRepository, ArtifactRepository artifactRepository) {
        this.folderRepository = folderRepository;
        this.artifactRepository = artifactRepository;
    }

    /**
     * Return all artifacts and (sub)folders for the given folder url.
     * @param url
     * @return
     */
    public Optional<List<Content>> findContentForFolder(String url) {
        if (folderRepository.existsByTotalUrl(url) || "/".equals(url)) {
            List<Content> content = new ArrayList<>();
            content.addAll(folderRepository.findByParentUrl(url));
            content.addAll(artifactRepository.findByParentUrl(url));
            return Optional.of(content);
        }
        return Optional.empty();
    }

    /**
     * Check if a potential folder or artifact name is dangerous.
     * This check prevents that we create artifact/folder urls which will be blocked by Spring HTTP firewall due to
     * security reasons.
     * @param name
     * @return
     */
    public boolean isNameDangerous(String name) {
        if (name == null || name.isEmpty() || name.contains("/")) {
            return true;
        }

        for (String unsafeString : UNSAFE_STRINGS) {
            if (name.contains(unsafeString)) {
                return true;
            }
        }
        return false;
    }
}
