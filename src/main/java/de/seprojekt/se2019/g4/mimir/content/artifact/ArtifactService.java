package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailContentStore;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailGenerator;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * This service offers helper methods for dealing with artifacts.
 */
@Service
public class ArtifactService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactService.class);
    private static long ONE_KB = 1000;
    private static long ONE_MB = ONE_KB * ONE_KB;
    private static long ONE_GB = ONE_KB * ONE_MB;
    private ArtifactRepository artifactRepository;
    private ArtifactContentStore artifactContentStore;
    private ThumbnailContentStore thumbnailContentStore;
    private ThumbnailGenerator thumbnailGenerator;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param artifactRepository
     * @param artifactContentStore
     * @param thumbnailContentStore
     * @param thumbnailGenerator
     */
    public ArtifactService(ArtifactRepository artifactRepository, ArtifactContentStore artifactContentStore, ThumbnailContentStore thumbnailContentStore, ThumbnailGenerator thumbnailGenerator) {
        this.artifactRepository = artifactRepository;
        this.artifactContentStore = artifactContentStore;
        this.thumbnailContentStore = thumbnailContentStore;
        this.thumbnailGenerator = thumbnailGenerator;
    }

    /**
     * Return all artifacts
     *
     * @return
     */
    public List<Artifact> findAll() {
        return artifactRepository.findAll();
    }

    /**
     * Return the artifact with the given total url.
     *
     * @param parentFolder
     * @return
     */
    public List<Artifact> findByParentFolder(Folder parentFolder) {
        return artifactRepository.findByParentFolder(parentFolder);
    }

    /**
     * Return the artifact with the given id.
     *
     * @param id
     * @return
     */
    public Optional<Artifact> findById(long id) {
        return artifactRepository.findById(id);
    }

    /**
     * Find and return the file of an artifact as an input stream.
     *
     * @param artifact
     * @return
     */
    public InputStream findArtifactContent(Artifact artifact) {
        return artifactContentStore.getContent(artifact);
    }

    /**
     * Find and return the thumbnail of an artifact as an input stream.
     *
     * @param artifact
     * @return
     */
    public Optional<InputStream> findThumbnail(Artifact artifact) {
        Thumbnail thumbnail = artifact.getThumbnail();
        InputStream inputStream = thumbnailContentStore.getContent(thumbnail);
        return Optional.ofNullable(inputStream);
    }

    /**
     * Check if an artifact exists
     *
     * @param parentFolder
     * @param displayName
     * @return
     */
    public boolean existsByParentFolderAndDisplayName(Folder parentFolder, String displayName) {
        return artifactRepository.existsByParentFolderAndDisplayName(parentFolder, displayName);
    }

    /**
     * Create a new artifact with a file.
     * This method will perform some checks (is file not empty?, is name not empty? is the name not dangerous? is the name
     * already given?).
     *
     * @param displayName
     * @param file
     * @param parentFolder
     * @param principal
     * @return
     * @throws IOException
     */
    @Transactional
    public Artifact upload(String displayName, MultipartFile file, Folder parentFolder, Principal principal) throws IOException {
        Artifact artifact = new Artifact();
        artifact.setDisplayName(displayName);
        artifact.setParentFolder(parentFolder);
        return saveWithFileAndThumbnail(artifact, file, principal);
    }

    /**
     * Creates/updates a artifact and create/update its file and its thumbnail.
     * Moreover, metadata like author, lastChange, contentType will be updated.
     * Please do not longer use the original artifact object, use the returned artifact object.
     *
     * @param artifact
     * @param file
     * @param principal
     * @return
     */
    private Artifact saveWithFileAndThumbnail(Artifact artifact, MultipartFile file, Principal principal) throws IOException {
        // TODO CHANGE AFTER USER MANAGEMENT IMPLEMENTATION
        principal = () -> "ROOT-USER";

        // force deletion of the file and thumbnail by unsetting and saving them
        artifactContentStore.unsetContent(artifact);
        thumbnailContentStore.unsetContent(artifact.getThumbnail());
        artifactRepository.save(artifact);

        // update metadata of artifact
        MediaType contentType = MediaType.valueOf(file.getContentType());
        artifact.setContentType(contentType);
        try (InputStream inputStream = file.getInputStream()) {
            artifactContentStore.setContent(artifact, inputStream);
        }
        artifact.setAuthor(principal.getName());
        artifact.setLastChange(Instant.now());
        Artifact updatedArtifact = artifactRepository.save(artifact);

        // generate thumbnail (beware - InputStreams are one-time-use only)
        try (InputStream storedContent = artifactContentStore.getContent(artifact)) {
            Optional<InputStream> possibleThumbnail = thumbnailGenerator.generateThumbnail(storedContent, artifact.getContentType());
            if (possibleThumbnail.isEmpty()) {
                return updatedArtifact;
            }
            try (InputStream thumbnail = possibleThumbnail.get()) {
                thumbnailContentStore.setContent(artifact.getThumbnail(), thumbnail);
                return artifactRepository.save(artifact);
            }
        }
    }

    /**
     * Deletes the artifacts with its file and thumbnail.
     * This method will perform some checks (is the artifact not locked?).
     *
     * @param artifact
     */
    @Transactional
    public void delete(Artifact artifact) {
        artifactContentStore.unsetContent(artifact);
        thumbnailContentStore.unsetContent(artifact.getThumbnail());
        // only after a repository.save(), contentStore.unsetContent() will delete the file -
        // repository.delete() won't trigger the deletion
        artifactRepository.save(artifact);
        artifactRepository.delete(artifact);
    }


    public String byteCountToDisplaySize(Artifact artifact) {
        String displaySize;
        long size = artifact.getContentLength();
        if (size / ONE_GB > 0) {
            displaySize = size / ONE_GB + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = size / ONE_MB + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = size / ONE_KB + " KB";
        } else {
            displaySize = size + " bytes";
        }
        return displaySize;
    }

    /**
     * If the display name does not have a file extension, then try to calculate the file extension from its content type.
     *
     * @param artifact
     * @return
     */
    public String calculateFileName(Artifact artifact) {
        String displayName = artifact.getDisplayName();
        if (!FilenameUtils.getExtension(displayName).isEmpty()) {
            return displayName;
        }

        String mimeType = artifact.getContentType().toString();
        try {
            String extension = TikaConfig.getDefaultConfig().getMimeRepository().forName(mimeType).getExtension();
            return FilenameUtils.getBaseName(displayName) + extension;
        } catch (MimeTypeException e) {
            LOGGER.error("cant find mimetype", e);
            // its not possible, simply return the display name
            return displayName;
        }
    }
}
