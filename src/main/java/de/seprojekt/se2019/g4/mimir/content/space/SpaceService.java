package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.security.Principal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpaceService.class);
    private SpaceRepository spaceRepository;
    private UserService userService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param spaceRepository
     */
    public SpaceService(SpaceRepository spaceRepository, UserService userService) {
        this.spaceRepository = spaceRepository;
        this.userService = userService;
    }

    /**
     * Return space with given id
     *
     * @param id
     * @return
     */
    public Optional<Space> findById(Long id) {
        return this.spaceRepository.findById(id);
    }

    /**
     * Return space with given root folder
     *
     * @param folder
     * @return
     */
    public Optional<Space> findByRootFolder(Folder folder) {
        return this.spaceRepository.findByRootFolder(folder);
    }


    /**
     * Update a space
     *
     * @param space
     * @return
     */
    @Transactional
    public Space update(Space space) {
        return spaceRepository.save(space);
    }

    /**
     * Return new space
     *
     * @param name
     * @param rootFolder
     * @param principal owner
     * @return
     */
    @Transactional
    public Space create(String name, Folder rootFolder, Principal principal) {
        Space space = new Space();
        space.setName(name);
        space.setRootFolder(rootFolder);

        space = spaceRepository.save(space);

        User user = userService.findByName(principal.getName()).get();
        user.getSpaces().add(space);
        userService.update(user);

        return space;
    }

    /**
     * Deletes a space
     * @param space
     */
    @Transactional
    public void delete(Space space) {
        this.spaceRepository.delete(space);
    }

    /**
     * Check if user is authorized for space
     *
     * @param space
     * @param principal
     * @return
     */
    @Transactional
    public boolean isAuthorizedForSpace(Space space, Principal principal) {
        Optional<User> optionalUser = this.userService.findByName(principal.getName());
        if(optionalUser.isEmpty()) {
            return false;
        }
        return optionalUser.get().getSpaces().contains(space);
    }

}
