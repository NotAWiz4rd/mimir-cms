package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.security.Principal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceService {

  private final static Logger LOGGER = LoggerFactory.getLogger(SpaceService.class);
  private SpaceRepository spaceRepository;
  private FolderService folderService;
  private UserService userService;

  /**
   * The parameters will be autowired by Spring.
   */
  public SpaceService(
      SpaceRepository spaceRepository,
      UserService userService,
      @Lazy FolderService folderService) {
    this.spaceRepository = spaceRepository;
    this.userService = userService;
    this.folderService = folderService;
  }

  /**
   * Return space with given id
   */
  public Optional<Space> findById(Long id) {
    return this.spaceRepository.findById(id);
  }

  /**
   * Return space with given root folder
   */
  public Optional<Space> findByRootFolder(Folder folder) {
    return this.spaceRepository.findByRootFolder(folder);
  }

  /**
   * Update a space
   */
  @Transactional
  public Space update(Space space) {
    return spaceRepository.save(space);
  }

  /**
   * Return new space
   *
   * @param principal owner
   */
  @Transactional
  public Space create(String name, Principal principal) {
    Folder rootFolder = folderService.create(null, name);

    Space space = new Space();
    space.setName(name);
    space.setRootFolder(rootFolder);

    space = spaceRepository.save(space);

    rootFolder.setSpace(space);
    this.folderService.update(rootFolder);

    User user = userService.findByName(principal.getName()).get();
    user.getSpaces().add(space);
    userService.update(user);

    return space;
  }

  /**
   * Deletes a space
   */
  @Transactional
  public void delete(Space space) {
    Folder rootFolder = space.getRootFolder();
    space.setRootFolder(null);
    space = this.update(space);

    userService.removeAllFromSpace(space);
    folderService.delete(rootFolder);
    this.spaceRepository.delete(space);
  }

}
