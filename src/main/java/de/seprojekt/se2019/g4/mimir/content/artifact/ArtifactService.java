package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.Thumbnail;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailGenerator;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
  private ThumbnailGenerator thumbnailGenerator;
  private ThumbnailRepository thumbnailRepository;
  private SpaceService spaceService;
  private FolderService folderService;

  /**
   * The parameters will be autowired by Spring.
   */
  public ArtifactService(
      ArtifactRepository artifactRepository,
      ThumbnailRepository thumbnailRepository,
      ThumbnailGenerator thumbnailGenerator,
      SpaceService spaceService,
      @Lazy FolderService folderService) {
    this.artifactRepository = artifactRepository;
    this.thumbnailRepository = thumbnailRepository;
    this.thumbnailGenerator = thumbnailGenerator;
    this.spaceService = spaceService;
    this.folderService = folderService;
  }

  /**
   * Return the artifact with the given parent folder
   */
  public List<Artifact> findByParentFolder(Folder parentFolder) {
    return artifactRepository.findByParentFolder(parentFolder);
  }

  /**
   * Check if an artifact with the given parent folder exists
   */
  public boolean existsByParentFolder(Folder parentFolder) {
    return artifactRepository.existsByParentFolder(parentFolder);
  }

  /**
   * Return the artifact with the given id.
   */
  public Optional<Artifact> findById(long id) {
    return artifactRepository.findById(id);
  }

  /**
   * Find and return the content of an artifact as an input stream.
   */
  public InputStream findArtifactContent(Artifact artifact) {
    return new ByteArrayInputStream(artifact.getContent());
  }

  /**
   * Find and return the thumbnail of an artifact as an input stream.
   */
  public Optional<InputStream> findThumbnail(Artifact artifact) {
    Thumbnail thumbnail = artifact.getThumbnail();
    return thumbnail.getContentLength() == null ? Optional.empty()
        : Optional.of(new ByteArrayInputStream(thumbnail.getContent()));
  }

  /**
   * Check if an artifact exists with a given name in a specific folder
   */
  public boolean existsByParentFolderAndDisplayName(Folder parentFolder, String name) {
    return artifactRepository.existsByParentFolderAndName(parentFolder, name);
  }

  /**
   * Update an artifact
   */
  @Transactional
  public Artifact update(Artifact artifact) {
    return artifactRepository.save(artifact);
  }

  /**
   * Create a new artifact with a file.
   */
  @Transactional
  public Artifact upload(String displayName, MultipartFile file, Folder parentFolder)
      throws IOException {
    Artifact artifact = new Artifact();
    artifact.setName(displayName);
    artifact.setParentFolder(parentFolder);
    artifact.setCreationDate(Instant.now());

    // update metadata of artifact
    MediaType contentType = MediaType.valueOf(file.getContentType());
    artifact.setContentType(contentType);
    artifact
        .setSpace(spaceService.findByRootFolder(folderService.getRootFolder(parentFolder)).get());

    // save artifact binary data
    try (InputStream inputStream = file.getInputStream()) {
      artifact.setContentLength(file.getSize());
      artifact.setContent(inputStream.readAllBytes());
    }
    Artifact updatedArtifact = artifactRepository.save(artifact);

    // generate thumbnail (beware - InputStreams are one-time-use only)
    try (InputStream storedContent = new ByteArrayInputStream(artifact.getContent())) {
      Optional<InputStream> possibleThumbnail = thumbnailGenerator
          .generateThumbnail(storedContent, artifact.getContentType());
      if (possibleThumbnail.isEmpty()) {
        return updatedArtifact;
      }
      try (InputStream thumbnailStream = possibleThumbnail.get()) {
        Thumbnail thumbnail = artifact.getThumbnail();
        byte[] thumbnailContent = thumbnailStream.readAllBytes();
        thumbnail.setContent(thumbnailContent);
        thumbnail.setContentLength((long) thumbnailContent.length);
        thumbnailRepository.save(thumbnail);
        return artifactRepository.save(artifact);
      }
    }
  }

  /**
   * Deletes the artifacts with its file and thumbnail. *
   */
  @Transactional
  public void delete(Artifact artifact) {
    thumbnailRepository.delete(artifact.getThumbnail());
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
   * If the name does not have a file extension, then try to calculate the file extension from its
   * content type.
   */
  public String calculateFileName(Artifact artifact) {
    String displayName = artifact.getName();
    if (!FilenameUtils.getExtension(displayName).isEmpty()) {
      return displayName;
    }

    String mimeType = artifact.getContentType().toString();
    try {
      String extension = TikaConfig.getDefaultConfig().getMimeRepository().forName(mimeType)
          .getExtension();
      return FilenameUtils.getBaseName(displayName) + extension;
    } catch (MimeTypeException e) {
      LOGGER.error("cant find mimetype", e);
      // its not possible, simply return the name
      return displayName;
    }
  }

}
