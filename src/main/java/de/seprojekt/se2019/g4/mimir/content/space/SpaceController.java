package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    private UserService userService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param spaceService
     */
    public SpaceController(SpaceService spaceService, FolderService folderService, UserService userService) {
        this.spaceService = spaceService;
        this.folderService = folderService;
        this.userService = userService;
    }

    /**
     * The user can get a list of all existing spaces using this interface.
     *
     * @return
     */
    @GetMapping(value = "/spaces")
    public ResponseEntity<List<Space>> getSpaces(Principal principal) {
        return ResponseEntity.ok().body(userService.findByName(principal.getName()).get().getSpaces());
    }

    /**
     * The user can get space DTO instance of the space with the provided id by calling this interface.
     * This instance contains the entire content tree of the space.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/space/{id}")
    public ResponseEntity<SpaceDTO> getSpace(@PathVariable long id, Principal principal) {
        Optional<Space> space = spaceService.findById(id);
        if (!space.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(space.get(), principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        spaceService.isAuthorizedForSpace(space.get(), principal);
        SpaceDTO spaceDTO = new SpaceDTO(space.get());
        spaceDTO.setRoot(folderService.getFolderDTOWithTree(space.get().getRootFolder()));
        return ResponseEntity.ok().body(spaceDTO);
    }

    /**
     * The user can create an space by calling this interface.
     *
     * @param name
     * @param principal
     * @return
     */
    @PostMapping(value = "/space")
    public ResponseEntity<Space> createSpace(@RequestParam("name") String name, Principal principal) {
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().build();
        }
        Folder rootFolder = folderService.create(null, name);
        Space space = spaceService.create(name, rootFolder, principal);
        rootFolder.setSpace(space);
        this.folderService.update(rootFolder);
        return ResponseEntity.ok().body(space);
    }

    /**
     * The user can delete a space by calling this interface.
     *
     * @param id
     * @param force
     * @return
     */
    @DeleteMapping(value = "/space/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") long id, @RequestParam(value = "force", required = false) String force, Principal principal) {
        Optional<Space> spaceOptional = spaceService.findById(id);
        if (spaceOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!spaceService.isAuthorizedForSpace(spaceOptional.get(), principal)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (spaceOptional.isPresent() && force == null && userService.getNumberOfSpaceUsers(spaceOptional.get()) > 1) {
            return ResponseEntity.status(409).body("Space has more than one user!");
        }
        if (spaceOptional.isPresent() && force == null && (!folderService.isEmpty(spaceOptional.get().getRootFolder()))) {
            return ResponseEntity.status(409).body("Space is not empty!");
        }
        Space space = spaceOptional.get();
        Folder rootFolder = space.getRootFolder();
        space.setRootFolder(null);
        space = spaceService.update(space);

        userService.removeAllFromSpace(space);
        folderService.delete(rootFolder);
        spaceService.delete(space);
        return ResponseEntity.ok().build();
    }
}
