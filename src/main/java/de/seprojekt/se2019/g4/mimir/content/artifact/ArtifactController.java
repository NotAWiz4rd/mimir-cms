package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;

/**
 * This controller offers an HTTP interface for manipulating artifacts (e.g. deleting, creating etc.)
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class ArtifactController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactController.class);

    private ArtifactService artifactService;
    private FolderService folderService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param artifactService
     * @param folderService
     */
    public ArtifactController(ArtifactService artifactService, FolderService folderService) {
        this.artifactService = artifactService;
        this.folderService = folderService;
    }

    /**
     * The user can get an artifact by calling this interface.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/artifact/{id}")
    public ResponseEntity<Artifact> getArtifact(@PathVariable long id) {
        Optional<Artifact> artifact = artifactService.findById(id);
        if (!artifact.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(artifact.get());
    }

    /**
     * Generate a download of an artifact when a user visit this url.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/artifact/{id}", params = "download")
    public ResponseEntity<InputStreamResource> downloadArtifact(@PathVariable long id) {
        Optional<Artifact> artifact = artifactService.findById(id);
        if (artifact.isEmpty()) {
            ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        String fileName = artifactService.calculateFileName(artifact.get());
        headers.set("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

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
     * The user can upload an artifact by calling this interface.
     *
     * @param principal
     * @param parentFolderId
     * @param name
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/artifact")
    public ResponseEntity<String> uploadArtifact(Principal principal, @RequestParam("parentId") Long parentFolderId, @RequestParam("name") String name, @RequestParam("file") MultipartFile file) throws IOException {
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().body("Name is empty!");
        }
        if (file == null || file.getContentType() == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty!");
        }
        Optional<Folder> parentFolder = folderService.findById(parentFolderId);
        if (parentFolder.isEmpty()) {
            return ResponseEntity.badRequest().body("Parent folder does not exist!");
        }
        if (artifactService.existsByParentFolderAndDisplayName(parentFolder.get(), name)) {
            return ResponseEntity.badRequest().body("File with same name already exists in this folder!");
        }
        artifactService.upload(name, file, parentFolder.get(), principal);
        return ResponseEntity.ok().body("Successfully uploaded new artifact");
    }

    /**
     * The user can delete an artifact by calling this interface.
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/artifact/{id}")
    public ResponseEntity<String> delete(@PathVariable long id) {
        Optional<Artifact> artifact = artifactService.findById(id);
        if (artifact.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        artifactService.delete(artifact.get());
        return ResponseEntity.ok().body("Successfully deleted artifact");
    }
}
