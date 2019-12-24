package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Controller
public class SpaceController {

  private final static Logger LOGGER = LoggerFactory.getLogger(SpaceController.class);
  private SpaceService spaceService;
  private FolderService folderService;
  private UserService userService;

  /**
   * The parameters will be autowired by Spring.
   */
  public SpaceController(
      SpaceService spaceService,
      FolderService folderService,
      UserService userService) {
    this.spaceService = spaceService;
    this.folderService = folderService;
    this.userService = userService;
  }

  /**
   * The user can get a list of all existing spaces using this interface. Endpoint usage is not
   * allowed for share tokens.
   */
  @GetMapping(value = "/spaces")
  public ResponseEntity<List<Space>> getSpaces(Principal principal) {
    if (JwtPrincipal.fromPrincipal(principal).isAnonymous()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().body(userService.findByName(principal.getName()).get().getSpaces());
  }

  /**
   * The user can get space DTO instance of the space with the provided id by calling this
   * interface. This instance contains the entire content tree of the space.
   */
  @GetMapping(value = "/space/{id}")
  public ResponseEntity<SpaceDTO> getSpace(@PathVariable long id, Principal principal) {
    Optional<Space> space = spaceService.findById(id);
    if (!space.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForSpace(space.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    SpaceDTO spaceDTO = new SpaceDTO(space.get());
    spaceDTO.setRoot(folderService.getFolderDTOWithTree(space.get().getRootFolder()));
    return ResponseEntity.ok().body(spaceDTO);
  }

  /**
   * The user can create an space by calling this interface.
   */
  @PostMapping(value = "/space")
  public ResponseEntity<Space> createSpace(@RequestParam("name") String name, Principal principal) {
    if (StringUtils.isEmpty(name)) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok().body(spaceService.create(name, principal));
  }

  /**
   * Adds a user to a space
   */
  @PutMapping(value = "/space/{id}")
  public ResponseEntity<String> addUser(@PathVariable long id,
      @RequestParam(name = "username") String username, Principal principal) {
    Optional<Space> space = spaceService.findById(id);
    Optional<User> user = userService.findByName(username);
    if (space.isEmpty() || user.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForSpace(space.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    userService.addUserToSpace(user.get(), space.get());
    return ResponseEntity.ok().build();
  }

  /**
   * Returns all users of a space.
   */
  @GetMapping(value = "/space/{id}/users")
  public ResponseEntity<List<User>> getUsers(@PathVariable long id, Principal principal) {
    Optional<Space> space = spaceService.findById(id);

    if (space.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    if (!userService.isAuthorizedForSpace(space.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(userService.getUsersBySpace(space.get()));
  }

  /**
   * Removes a Space from a User.
   */
  @DeleteMapping(value = "space/{id}/removeuser")
  public ResponseEntity<String> removeUser(@PathVariable long id,
      @RequestParam(name = "id") long userId, Principal principal) {
    Optional<Space> space = spaceService.findById(id);
    Optional<User> user = userService.findById(userId);

    if (space.isEmpty() || user.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    if (!userService.isAuthorizedForSpace(space.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    User realUser = user.get();
    realUser.getSpaces().remove(space.get());
    userService.update(realUser);
    return ResponseEntity.ok().build();
  }

  /**
   * The user can delete a space by calling this interface.
   */
  @DeleteMapping(value = "/space/{id}")
  public ResponseEntity<String> delete(@PathVariable("id") long id,
      @RequestParam(value = "force", required = false) String force, Principal principal) {
    Optional<Space> spaceOptional = spaceService.findById(id);
    if (spaceOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    if (!userService.isAuthorizedForSpace(spaceOptional.get(), principal)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (spaceOptional.isPresent() && force == null
        && userService.getNumberOfSpaceUsers(spaceOptional.get()) > 1) {
      return ResponseEntity.status(409).body("Space has more than one user!");
    }
    if (spaceOptional.isPresent() && force == null && (!folderService
        .isEmpty(spaceOptional.get().getRootFolder()))) {
      return ResponseEntity.status(409).body("Space is not empty!");
    }
    spaceService.delete(spaceOptional.get());
    return ResponseEntity.ok().build();
  }
}
