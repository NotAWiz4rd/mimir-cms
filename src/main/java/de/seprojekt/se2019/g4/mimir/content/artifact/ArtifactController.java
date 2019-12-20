package de.seprojekt.se2019.g4.mimir.content.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.security.JwtTokenProvider;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * This controller offers an HTTP interface for manipulating artifacts (e.g. deleting, creating
 * etc.)
 */
@Controller
public class ArtifactController {

  private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactController.class);

  private ArtifactService artifactService;
  private FolderService folderService;
  private JwtTokenProvider jwtTokenProvider;
  private UserService userService;

  /**
   * The parameters will be autowired by Spring.
   */
  public ArtifactController(
      ArtifactService artifactService,
      FolderService folderService,
      JwtTokenProvider jwtTokenProvider,
      UserService userService) {
    this.artifactService = artifactService;
    this.folderService = folderService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.userService = userService;
  }

  /**
   * The user can get an artifact by calling this interface.
   */
  @GetMapping(value = "/artifact/{id}")
  public ResponseEntity<Artifact> getArtifact(@PathVariable long id, Principal principal) {
    Optional<Artifact> artifact = artifactService.findById(id);
    if (!artifact.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForArtifact(artifact.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().body(artifact.get());
  }

  /**
   * The user can get an JWT for sharing this artifact, if he has access to the space containing
   * this artifact
   */
  @GetMapping(value = "/artifact/share/{id}")
  public ResponseEntity<String> getShareToken(@PathVariable long id,
      @RequestParam(name = "expiration", required = false) Integer expirationMs,
      Principal principal)
      throws JsonProcessingException {
    Optional<Artifact> artifact = artifactService.findById(id);
    if (artifact.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForSpace(artifact.get().getSpace(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().body(jwtTokenProvider
        .generateShareToken(artifact.get().getId(), Artifact.TYPE_IDENTIFIER, expirationMs));
  }

  /**
   * The user can get an JWT (with short expiration time) for downloading an artifact
   */
  @GetMapping(value = "/artifact/download/{id}")
  public ResponseEntity<String> getDownloadToken(@PathVariable long id, Principal principal)
      throws JsonProcessingException {
    Optional<Artifact> artifact = artifactService.findById(id);
    if (artifact.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForArtifact(artifact.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().body(jwtTokenProvider
        .generateDownloadToken(artifact.get().getId(), Artifact.TYPE_IDENTIFIER));
  }

  /**
   * Generate a download of an artifact when a user visit this url
   */
  @GetMapping(value = "/artifact/{id}/download")
  public ResponseEntity<InputStreamResource> downloadArtifact(@PathVariable long id, Principal principal) {
    Optional<Artifact> artifact = artifactService.findById(id);
    if (artifact.isEmpty()) {
      ResponseEntity.notFound().build();
    }

    if (!userService.isAuthorizedForArtifact(artifact.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Disposition",
        String.format("attachment; filename=\"%s\"", artifact.get().getName()));

    InputStream inputStream = artifactService.findArtifactContent(artifact.get());
    InputStreamResource resource = new InputStreamResource(inputStream);
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(artifact.get().getContentType())
        .contentLength(artifact.get().getContentLength())
        // InputStreamResource will close the InputStream
        .body(resource);
  }

  /**
   * returns raw content of artifact when a user visits this url
   */
  @GetMapping(value = "/artifact/{id}/raw")
  public ResponseEntity getRawData(@PathVariable Long id, Principal principal) {
    Optional<Artifact> artifactOptional = artifactService.findById(id);
    if (artifactOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Artifact artifact = artifactOptional.get();
    if (!userService.isAuthorizedForArtifact(artifact, principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    InputStream inputStream = artifactService.findArtifactContent(artifact);
    InputStreamResource content = new InputStreamResource(inputStream);
    return ResponseEntity.ok()
        .contentType(artifact.getContentType())
        .contentLength(artifact.getContentLength())
        .body(content);
  }

  /**
   * The user can upload an artifact by calling this interface.
   */
  @PostMapping(value = "/artifact")
  public ResponseEntity<Artifact> uploadArtifact(@RequestParam("parentId") Long parentFolderId,
      @RequestParam("name") String name, @RequestParam("file") MultipartFile file,
      Principal principal) throws IOException {
    Optional<Folder> parentFolder = folderService.findById(parentFolderId);
    if (parentFolder.isEmpty()) {
      return ResponseEntity.status(409).build();
    }
    if (!userService.isAuthorizedForFolder(parentFolder.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (artifactService.existsByParentFolderAndDisplayName(parentFolder.get(), name)) {
      return ResponseEntity.status(409).build();
    }
    if (StringUtils.isEmpty(name)) {
      return ResponseEntity.badRequest().build();
    }
    if (file == null || file.getContentType() == null || file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok().body(artifactService.upload(name, file, parentFolder.get()));
  }

  /**
   * The user can rename an artifact by calling this interface
   */
  @PutMapping(value = "/artifact/{id}")
  public ResponseEntity<Artifact> renameArtifact(@PathVariable long id,
      @RequestParam("name") String name, Principal principal) {
    Optional<Artifact> artifactOptional = artifactService.findById(id);
    if (artifactOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForArtifact(artifactOptional.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (StringUtils.isEmpty(name)) {
      return ResponseEntity.badRequest().build();
    }
    Artifact artifact = artifactOptional.get();
    artifact.setName(name);
    return ResponseEntity.ok().body(artifactService.update(artifact));
  }

  /**
   * The user can delete an artifact by calling this interface.
   */
  @DeleteMapping(value = "/artifact/{id}")
  public ResponseEntity<String> delete(@PathVariable long id, Principal principal) {
    Optional<Artifact> artifact = artifactService.findById(id);
    if (artifact.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForArtifact(artifact.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    artifactService.delete(artifact.get());
    return ResponseEntity.ok().build();
  }
}
