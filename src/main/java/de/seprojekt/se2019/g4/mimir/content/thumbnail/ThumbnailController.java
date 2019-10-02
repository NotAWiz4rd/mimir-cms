package de.seprojekt.se2019.g4.mimir.content.thumbnail;

import de.seprojekt.se2019.g4.mimir.content.Content;
import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.icon.IconDiscoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;

/**
 * This controller will offer an HTTP interface for the web page to load thumbnails.
 */
@Controller
public class ThumbnailController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ThumbnailController.class);
    private IconDiscoverService iconDiscoverService;
    private ArtifactService artifactService;

    /**
     * The parameters will be autowired by Spring.
     * @param iconDiscoverService
     * @param artifactService
     */
    public ThumbnailController(IconDiscoverService iconDiscoverService, ArtifactService artifactService) {
        this.iconDiscoverService = iconDiscoverService;
        this.artifactService = artifactService;
    }

    /**
     * This is a helper method for the Thymeleaf engine. In some templates, we have to check, if an artifact
     * should have an thumbnail or an icon.
     * @param content
     * @return
     */
    public static String getLinkToSymbolOrThumbnail(Content content) {
        if (ThumbnailGenerator.supportMimeType(content.getContentType())) {
            return "/thumbnail/" + content.getId();
        }
        String encodedType = content.getContentType().toString().replace("/", "-");
        return "/icon/" + encodedType;
    }

    /**
     * This method will be called, when the web page will access the given url and match certain criteria.
     * It will return the thumbnail for the artifact with the given id.
     * @param id
     * @return
     */
    @GetMapping(value = "/thumbnail/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity getThumbnail(@PathVariable Long id) {
        Artifact artifact = artifactService.findById(id).orElseThrow(EntityNotFoundException::new);
        return artifactService.findThumbnail(artifact)
                .map(thumbnailInputStream -> returnRealThumbnail(thumbnailInputStream, artifact))
                .orElseGet(() -> fallbackToIcon(artifact.getContentType()));
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
