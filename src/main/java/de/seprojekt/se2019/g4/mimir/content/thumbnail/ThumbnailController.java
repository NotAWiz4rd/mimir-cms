package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.util.Optional;

/**
 * This controller will offer an HTTP interface for the security page to load thumbnails.
 */
@Controller
public class ThumbnailController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ThumbnailController.class);
    private IconDiscoverService iconDiscoverService;
    private ArtifactService artifactService;
    private SpaceService spaceService;

    /**
     * The parameters will be autowired by Spring.
     * @param iconDiscoverService
     * @param artifactService
     */
    public ThumbnailController(IconDiscoverService iconDiscoverService, ArtifactService artifactService, SpaceService spaceService) {
        this.iconDiscoverService = iconDiscoverService;
        this.artifactService = artifactService;
        this.spaceService = spaceService;
    }

    /**
     * This method will be called, when the security page will access the given url and match certain criteria.
     * It will return the thumbnail for the artifact with the given id.
     * @param id
     * @return
     */
    @GetMapping(value = "/thumbnail/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity getThumbnail(@PathVariable Long id, Principal principal) {
        Optional<Artifact> artifact = artifactService.findById(id);
        if(artifact.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(artifact.get().getSpace(), principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return artifactService.findThumbnail(artifact.get())
                .map(thumbnailInputStream -> returnRealThumbnail(thumbnailInputStream, artifact.get()))
                .orElseGet(() -> fallbackToIcon(artifact.get().getContentType()));
    }

    /**
     * Return the real thumbnail with the correct contentType etc.
     * @param thumbnailInputStream
     * @return
     */
    private ResponseEntity returnRealThumbnail(InputStream thumbnailInputStream, Artifact artifact) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(artifact.getThumbnail().getContentLength())
                // InputStreamResource will close the InputStream
                .body(new InputStreamResource(thumbnailInputStream));
    }

    /**
     * Return the icon for the given mediaType as fallback thumbnail.
     * @param mediaType
     * @return
     */
    private ResponseEntity fallbackToIcon(MediaType mediaType) {
        ClassPathResource resource = iconDiscoverService.loadIconFor(mediaType).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(resource);
    }
}
