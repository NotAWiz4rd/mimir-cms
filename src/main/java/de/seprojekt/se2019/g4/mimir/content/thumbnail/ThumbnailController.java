package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityNotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller will offer an HTTP interface for the security page to load thumbnails.
 */
@Controller
public class ThumbnailController {

  private IconDiscoverService iconDiscoverService;
  private ArtifactService artifactService;
  private UserService userService;

  /**
   * The parameters will be autowired by Spring.
   */
  public ThumbnailController(
      IconDiscoverService iconDiscoverService,
      ArtifactService artifactService,
      UserService userService) {
    this.iconDiscoverService = iconDiscoverService;
    this.artifactService = artifactService;
    this.userService = userService;
  }

  /**
   * This method will be called, when the security page will access the given url and match certain
   * criteria. It will return the thumbnail for the artifact with the given id.
   */
  @GetMapping(value = "/thumbnail/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
  @ResponseBody
  public ResponseEntity getThumbnail(@PathVariable Long id, Principal principal) {
    Optional<Artifact> artifact = artifactService.findById(id);
    if (artifact.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForArtifact(artifact.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return artifactService.findThumbnail(artifact.get())
        .map(thumbnailInputStream -> returnRealThumbnail(thumbnailInputStream, artifact.get()))
        .orElseGet(() -> fallbackToIcon(artifact.get().getContentType()));
  }

  /**
   * Return the real thumbnail with the correct contentType etc.
   */
  private ResponseEntity returnRealThumbnail(InputStream thumbnailInputStream, Artifact artifact) {
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .contentLength(artifact.getThumbnail().getContentLength())
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
        // InputStreamResource will close the InputStream
        .body(new InputStreamResource(thumbnailInputStream));
  }

  /**
   * Return the icon for the given mediaType as fallback thumbnail.
   */
  private ResponseEntity fallbackToIcon(MediaType mediaType) {
    ClassPathResource resource = iconDiscoverService.loadIconFor(mediaType)
        .orElseThrow(EntityNotFoundException::new);
    return ResponseEntity.ok()
        .contentType(MediaType.valueOf("image/svg+xml"))
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
        .body(resource);
  }
}
