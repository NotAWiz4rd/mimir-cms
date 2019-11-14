package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class SpaceController {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpaceController.class);
    private SpaceService spaceService;
    private FolderService folderService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param spaceService
     */
    public SpaceController(SpaceService spaceService, FolderService folderService) {
        this.spaceService = spaceService;
        this.folderService = folderService;
    }

    /**
     * The user can get a list of all existing spaces using this interface.
     *
     * @return
     */
    @GetMapping(value = "/spaces")
    public ResponseEntity<List<Space>> getSpaces() {
        return ResponseEntity.ok().body(spaceService.findAll());
    }

    /**
     * The user can get space helper instance of the space with the provided id by calling this interface.
     * This instance contains the entire content tree of the space.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/space/{id}")
    public ResponseEntity<SpaceHelper> getSpace(@PathVariable long id) {
        Optional<Space> space = spaceService.findById(id);
        if (!space.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        SpaceHelper spaceHelper = new SpaceHelper(space.get());
        spaceHelper.setRoot(folderService.getFolderHelperWithTree(space.get().getRootFolder()));
        return ResponseEntity.ok().body(spaceHelper);
    }

    /**
     * The user can create an space by calling this interface.
     *
     * @param principal
     * @param name
     * @return
     */
    @PostMapping(value = "/space")
    public ResponseEntity<Space> createSpace(Principal principal, @RequestParam("name") String name) {
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().build();
        }
        Folder rootFolder = folderService.create(null, name);
        return ResponseEntity.ok().body(spaceService.create(name, rootFolder, principal));
    }

    /**
     * The user can delete a space by calling this interface.
     */
    @DeleteMapping(value = "/space/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") long id) {
        Optional<Space> space = spaceService.findById(id);
        if (space.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (space.isPresent() && !folderService.isEmpty(space.get().getRootFolder())) {
            return ResponseEntity.status(409).body("Space is not empty!");
        }
        spaceService.delete(space.get());
        folderService.delete(space.get().getRootFolder());
        return ResponseEntity.ok().build();
    }
}
