package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpaceService.class);
    private SpaceRepository spaceRepository;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param spaceRepository
     */
    public SpaceService(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    /**
     * Return list of all spaces
     *
     * @return
     */
    public List<Space> findAll() {
        return this.spaceRepository.findAll();
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
     * Return new space
     *
     * @param name
     * @param rootFolder
     * @param principal owner
     * @return
     */
    @Transactional
    public Space create(String name, Folder rootFolder, Principal principal) {
        // TODO CHANGE AFTER USER MANAGEMENT IMPLEMENTATION
        principal = () -> "ROOT-USER";

        Space space = new Space();
        space.setName(name);
        space.setRootFolder(rootFolder);
        space.setOwner(principal.getName());
        return spaceRepository.save(space);
    }

    /**
     * Deletes a space
     * @param space
     */
    @Transactional
    public void delete(Space space) {
        this.spaceRepository.delete(space);
    }

}
