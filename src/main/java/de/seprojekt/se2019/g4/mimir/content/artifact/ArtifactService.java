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
   * Create
   */
  @Transactional
  public Artifact create(String displayName, String author, MultipartFile file, Folder parentFolder)
      throws IOException {
    Artifact artifact = new Artifact();
    artifact.setName(displayName);
    artifact.setAuthor(author);
    artifact.setParentFolder(parentFolder);
    artifact.setCreationDate(Instant.now());
    return this.upload(artifact, file);
  }

  /**
   * Create a new artifact with a file.
   */
  @Transactional
  public Artifact upload(Artifact artifact, MultipartFile file)
      throws IOException {
    LOGGER.info("Saving artifact '{}'", artifact.getName());

    // update metadata of artifact
    MediaType contentType = MediaType.valueOf(file.getContentType());
    artifact.setContentType(contentType);
    artifact
        .setSpace(
            spaceService.findByRootFolder(folderService.getRootFolder(artifact.getParentFolder()))
                .get());

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
    LOGGER.info("Deleting artifact '{}'", artifact.getName());
    thumbnailRepository.delete(artifact.getThumbnail());
    artifactRepository.delete(artifact);
  }

}
