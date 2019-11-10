package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * This controller offers an HTTP interface for manipulating folders (e.g. deleting, creating etc.)
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class FolderController {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderController.class);
    private FolderService folderService;
    private ArtifactService artifactService;
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param folderService
     * @param artifactService
     * @param applicationBaseUrl
     */
    public FolderController(FolderService folderService, ArtifactService artifactService, @Value("${application.base.url}") String applicationBaseUrl) {
        this.folderService = folderService;
        this.artifactService = artifactService;
        this.applicationBaseUrl = applicationBaseUrl;
    }

    /**
     * The user can get a list of all root folders using this interface.
     *
     * @return
     */
    @GetMapping(value = "/folder/root")
    public ResponseEntity<List<Folder>> getRootFolder() {
        return ResponseEntity.ok().body(folderService.findRootFolder());
    }

    /**
     * The user can get the content of a folder by calling this interface.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/folder/{id}")
    public ResponseEntity<FolderHelper> getFolderContent(@PathVariable long id) {
        Optional<Folder> folder = folderService.findById(id);
        if (!folder.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        FolderHelper folderHelper = new FolderHelper(folder.get());
        folderHelper.setFolders(folderService.findByParentFolder(folder.get()));
        folderHelper.setArtifacts(artifactService.findByParentFolder(folder.get()));
        return ResponseEntity.ok().body(folderHelper);
    }

    /**
     * The user can create a new folder by calling this interface.
     *
     * @param name
     * @param parentFolderId
     * @return
     */
    @PostMapping(value = "/folder")
    public ResponseEntity<String> create(@RequestParam("name") String name, @RequestParam("parentId") Long parentFolderId) {
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().body("Name is empty!");
        }
        Optional<Folder> parentFolder = folderService.findById(parentFolderId);
        if (parentFolder.isEmpty()) {
            return ResponseEntity.badRequest().body("ParentFolder not found!");
        }
        if (parentFolder.isPresent() && folderService.exists(parentFolder.get(), name)) {
            return ResponseEntity.badRequest().body("Folder with same name already exists!");
        }
        folderService.create(parentFolder.get(), name);
        return ResponseEntity.ok().body("Successfully created new folder!");
    }

    /**
     * The user can delete a folder by calling this interface.
     */
    @DeleteMapping(value = "/folder/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") long id) {
        Optional<Folder> folder = folderService.findById(id);
        if (folder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (folder.isPresent() && !folderService.isEmpty(folder.get())) {
            return ResponseEntity.badRequest().body("Folder is not empty!");
        }
        folderService.delete(folder.get());
        return ResponseEntity.ok().body("Successfully deleted folder!");
    }
}
