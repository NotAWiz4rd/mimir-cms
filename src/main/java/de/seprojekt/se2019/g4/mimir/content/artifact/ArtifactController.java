package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderDTO;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.security.JwtTokenProvider;

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
public class ArtifactController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactController.class);

    private ArtifactService artifactService;
    private FolderService folderService;
    private JwtTokenProvider jwtTokenProvider;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param artifactService
     * @param folderService
     */
    public ArtifactController(ArtifactService artifactService, FolderService folderService, JwtTokenProvider jwtTokenProvider) {
        this.artifactService = artifactService;
        this.folderService = folderService;
        this.jwtTokenProvider = jwtTokenProvider;
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
     * Not secured by Spring Security!
     *
     * @param id
     * @return
     */
    @PostMapping(value = "/artifact/{id}/download")
    public ResponseEntity<InputStreamResource> downloadArtifact(@PathVariable long id, @RequestParam(required=true, name="token") String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Artifact> artifact = artifactService.findById(id);
        if (artifact.isEmpty()) {
            ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", String.format("attachment; filename=\"%s\"", artifact.get().getName()));

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
    public ResponseEntity<Artifact> uploadArtifact(Principal principal, @RequestParam("parentId") Long parentFolderId, @RequestParam("name") String name, @RequestParam("file") MultipartFile file) throws IOException {
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().build();
        }
        if (file == null || file.getContentType() == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Folder> parentFolder = folderService.findById(parentFolderId);
        if (parentFolder.isEmpty()) {
            return ResponseEntity.status(409).build();
        }
        if (artifactService.existsByParentFolderAndDisplayName(parentFolder.get(), name)) {
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.ok().body(artifactService.upload(name, file, parentFolder.get(), principal));
    }

    /**
     * The user can rename an artifact by calling this interface
     *
     * @param id
     * @param name
     * @return
     */
    @PutMapping(value = "/artifact/{id}")
    public ResponseEntity<Artifact> renameArtifact(@PathVariable long id, @RequestParam("name") String name) {
        Optional<Artifact> artifactOptional = artifactService.findById(id);
        if (artifactOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
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
        return ResponseEntity.ok().build();
    }
}
