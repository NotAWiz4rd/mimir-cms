package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.ContentService;
import de.seprojekt.se2019.g4.mimir.content.DisplayableException;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailContentStore;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailGenerator;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * This service offers helper methods for dealing with artifacts.
 */
@Service
public class ArtifactService {
    private static long ONE_KB = 1000;
    private static long ONE_MB = ONE_KB * ONE_KB;
    private static long ONE_GB = ONE_KB * ONE_MB;

    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactService.class);

    private ArtifactRepository artifactRepository;
    private ArtifactContentStore artifactContentStore;
    private ThumbnailContentStore thumbnailContentStore;
    private ThumbnailGenerator thumbnailGenerator;
    private ContentService contentService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param artifactRepository
     * @param artifactContentStore
     * @param thumbnailContentStore
     * @param thumbnailGenerator
     * @param contentService
     */
    public ArtifactService(ArtifactRepository artifactRepository, ArtifactContentStore artifactContentStore, ThumbnailContentStore thumbnailContentStore, ThumbnailGenerator thumbnailGenerator, ContentService contentService) {
        this.artifactRepository = artifactRepository;
        this.artifactContentStore = artifactContentStore;
        this.thumbnailContentStore = thumbnailContentStore;
        this.thumbnailGenerator = thumbnailGenerator;
        this.contentService = contentService;
    }

    /**
     * Return the artifact with the given total url.
     *
     * @param totalUrl
     * @return
     */
    public Optional<Artifact> findByTotalUrl(String totalUrl) {
        return artifactRepository.findByTotalUrl(totalUrl);
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
    public InputStream findArtifact(Artifact artifact) {
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
     * Check if an artifact with the given total url exists.
     *
     * @param totalUrl
     * @return
     */
    public boolean existsByTotalUrl(String totalUrl) {
        return artifactRepository.existsByTotalUrl(totalUrl);
    }

    /**
     * Create a new artifact with a file.
     * This method will perform some checks (is file not empty?, is name not empty? is the name not dangerous? is the name
     * already given?).
     *
     * @param name
     * @param file
     * @param currentUrl
     * @param principal
     * @return
     * @throws IOException
     */
    @Transactional
    public Artifact initialCheckin(String name, MultipartFile file, String currentUrl, Principal principal) throws IOException {
        if (file == null || file.getContentType() == null || file.isEmpty()) {
            throw new DisplayableException("Bitte wähle eine Datei für den initialen CheckIn aus.");
        }

        if (StringUtils.isEmpty(name)) {
            name = FilenameUtils.removeExtension(file.getOriginalFilename());
        }
        if (StringUtils.isEmpty(name)) {
            throw new DisplayableException("Bitte gebe einen Namen für den initialen CheckIn an.");
        }
        if (contentService.isNameDangerous(name)) {
            throw new DisplayableException("Der Artefaktname darf keine verbotene Zeichen enthalten.");
        }

        String totalUrl = currentUrl + name;
        if (existsByTotalUrl(totalUrl)) {
            throw new DisplayableException("Der Name ist schon vergeben.");
        }

        if (totalUrl.length() > 300) {
            throw new DisplayableException("Der Name ist zu lang.");
        }

        Artifact artifact = new Artifact();
        artifact.setDisplayName(name);
        artifact.setParentUrl(currentUrl);
        artifact.setTotalUrl(totalUrl);
        return saveWithFileAndThumbnail(artifact, file, principal);
    }

    /**
     * Checkin a new file for an existing artifact.
     * This method will perform some checks (is the file not empty? is the file only locked by the current user?).
     *
     * @param id
     * @param newFile
     * @return
     */
    @Transactional
    public Artifact checkin(long id, MultipartFile newFile, Principal principal) throws IOException {
        Artifact artifact = findById(id).orElseThrow(EntityNotFoundException::new);
        if (!isLockedByCurrentUser(artifact)) {
            throw new DisplayableException("Artefakt ist nicht vom aktuellen Benutzer gesperrt.");
        }

        if (newFile == null || newFile.getContentType() == null || newFile.isEmpty()) {
            throw new DisplayableException("Bitte wähle eine Datei für den CheckIn aus.");
        }

        artifact.setLocked(false);
        artifact.setLockedByName("");
        return saveWithFileAndThumbnail(artifact, newFile, principal);
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
     * Checkout an existing artifact.
     * This method will perform some checks (is the artifact not locked?).
     *
     * @param artifact
     * @return
     */
    @Transactional
    public Artifact checkout(Artifact artifact, Principal principal) {
        if (artifact.isLocked()) {
            throw new DisplayableException("Das Artefakt kann nicht ausgecheckt werden, weil es gesperrt ist.");
        }
        artifact.setLocked(true);
        artifact.setLockedByName(principal.getName());
        return artifactRepository.save(artifact);
    }

    /**
     * Unlock the artifact.
     * This method will perform some checks (is the artifact locked?).
     *
     * @param artifact
     * @return
     */
    @Transactional
    public Artifact forceUnlock(Artifact artifact) {
        if (!artifact.isLocked()) {
            throw new DisplayableException("Das Artefakt kann nicht entsperrt werden, weil es nicht gesperrt ist.");
        }
        artifact.setLocked(false);
        artifact.setLockedByName("");
        return artifactRepository.save(artifact);
    }

    /**
     * Deletes the artifacts with its file and thumbnail.
     * This method will perform some checks (is the artifact not locked?).
     *
     * @param artifact
     */
    @Transactional
    public void delete(Artifact artifact) {
        if (artifact.isLocked()) {
            throw new DisplayableException("Das Artefakt kann nicht gelöscht werden, weil es gesperrt ist.");
        }

        artifactContentStore.unsetContent(artifact);
        thumbnailContentStore.unsetContent(artifact.getThumbnail());
        // only after a repository.save(), contentStore.unsetContent() will delete the file -
        // repository.delete() won't trigger the deletion
        artifactRepository.save(artifact);
        artifactRepository.delete(artifact);
    }

    /**
     * Rename an artifact (by its id) and check, if the new name is available.
     * Please do not longer use the original artifact object, use the returned artifact object.
     *
     * @param artifactId
     * @param newName
     * @return
     */
    @Transactional
    public Artifact rename(long artifactId, String newName) {
        Artifact artifact = artifactRepository.findById(artifactId).orElseThrow(EntityNotFoundException::new);
        String totalUrl = artifact.getParentUrl() + newName;

        if (artifact.isLocked()) {
            throw new DisplayableException("Das Artefakt kann nicht umbenannt werden, weil es gesperrt ist.");
        }
        if (contentService.isNameDangerous(newName)) {
            throw new DisplayableException("Der Artefaktname darf keine verbotene Zeichen enthalten.");
        }
        if (artifactRepository.existsByTotalUrl(totalUrl)) {
            throw new DisplayableException("cant rename because new name is already used");
        }
        if (totalUrl.length() > 300) {
            throw new DisplayableException("Der Name ist zu lang.");
        }

        artifact.setTotalUrl(totalUrl);
        artifact.setDisplayName(newName);
        return artifactRepository.save(artifact);
    }

    /**
     * Check if the given artifact is locked only from the current user (and not someone else).
     *
     * @param artifact
     * @return
     */
    public boolean isLockedByCurrentUser(Artifact artifact) {
        if (!artifact.isLocked()) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return Objects.equals(artifact.getLockedByName(), authentication.getName());
    }

    /**
     * Return the size of an artifact in KB, MB, GB (base 10) and not KiBi, MiBi, GiBi (base 2) like FileUtils.
     * Copied and modified from:
     * https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/FileUtils.java#L380 (Apache License 2.0)
     *
     * @param artifact
     * @return
     */
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
