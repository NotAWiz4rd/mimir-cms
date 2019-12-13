package de.seprojekt.se2019.g4.mimir.content.folder;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
import de.seprojekt.se2019.g4.mimir.security.JwtTokenProvider;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This controller offers an HTTP interface for manipulating folders (e.g. deleting, creating etc.)
 */
@Controller
public class FolderController {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderController.class);
    private FolderService folderService;
    private ArtifactService artifactService;
    private JwtTokenProvider jwtTokenProvider;
    private SpaceService spaceService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param folderService
     * @param artifactService
     */
    public FolderController(FolderService folderService, ArtifactService artifactService, JwtTokenProvider jwtTokenProvider, SpaceService spaceService) {
        this.folderService = folderService;
        this.artifactService = artifactService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.spaceService = spaceService;
    }

    /**
     * The user can get a folder by calling this interface.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/folder/{id}")
    public ResponseEntity<FolderDTO> getFolderContent(@PathVariable long id, Principal principal) {
        Optional<Folder> folder = folderService.findById(id);
        if (folder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(folder.get().getSpace(), principal)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        FolderDTO folderDTO = folderService.getFolderDTOWithTree(folder.get());
        return ResponseEntity.ok().body(folderDTO);
    }

    /**
     * The user can get an JWT for sharing this folder
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/folder/share/{id}")
    public ResponseEntity<String> getShareToken(@PathVariable long id, @RequestParam(name = "expiration", required = false) Integer expirationMs, Principal principal)
        throws JsonProcessingException {
        Optional<Folder> folder = folderService.findById(id);
        if (folder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(folder.get().getSpace(), principal)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().body(jwtTokenProvider.generateShareToken(folder.get().getId(), Folder.TYPE_IDENTIFIER, expirationMs));
    }

    /**
     * Generated a download of a folder as a ZIP file
     * Not secured by Spring Security!
     *
     * @param id
     * @return
     */
    @PostMapping(value = "/folder/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFolder(@PathVariable long id, @RequestParam(required=true, name="token") String token) throws IOException {
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Folder> folder = folderService.findById(id);
        if (folder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(folder.get().getSpace(), () -> jwtTokenProvider.getPayload(token, "sub"))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", String.format("attachment; filename=\"%s\"", folder.get().getName() + ".zip"));

        InputStream inputStream = this.folderService.zip(folder.get());
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.asMediaType(MimeType.valueOf("application/zip")))
                .contentLength(inputStream.available())
                // InputStreamResource will close the InputStream
                .body(resource);
    }

    /**
     * The user can create a new folder by calling this interface.
     *
     * @param name
     * @param parentFolderId
     * @return
     */
    @PostMapping(value = "/folder")
    public ResponseEntity<Folder> create(@RequestParam("name") String name, @RequestParam("parentId") Long parentFolderId, Principal principal) {
        Optional<Folder> parentFolder = folderService.findById(parentFolderId);
        if (parentFolder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(parentFolder.get().getSpace(), principal)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().build();
        }
        if (parentFolder.isPresent() && folderService.exists(parentFolder.get(), name)) {
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.ok().body(folderService.create(parentFolder.get(), name));
    }

    /**
     * The user can rename a folder by calling this interface
     *
     * @param id
     * @param name
     * @return
     */
    @PutMapping(value = "/folder/{id}")
    public ResponseEntity<FolderDTO> renameFolder(@PathVariable long id, @RequestParam("name") String name, Principal principal) {
        Optional<Folder> folderOptional = folderService.findById(id);
        if (folderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(folderOptional.get().getSpace(), principal)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().build();
        }
        Folder folder = folderOptional.get();
        folder.setName(name);
        folder = folderService.update(folder);
        FolderDTO folderDTO = folderService.getFolderDTOWithTree(folder);
        return ResponseEntity.ok().body(folderDTO);
    }

    /**
     * The user can delete a folder by calling this interface.
     *
     * @param id
     * @param force
     * @return
     */
    @DeleteMapping(value = "/folder/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") long id, @RequestParam(value = "force", required = false) String force, Principal principal) {
        Optional<Folder> folder = folderService.findById(id);
        if (folder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(folder.get().getSpace(), principal)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (force == null && !folderService.isEmpty(folder.get())) {
            return ResponseEntity.status(409).body("Folder is not empty!");
        }
        if (force != null && folder.get().getParentFolder() == null) {
            return ResponseEntity.status(409).body("Folder is a root folder. Delete the space instead!");
        }
        folderService.delete(folder.get());
        return ResponseEntity.ok().build();
    }

}
