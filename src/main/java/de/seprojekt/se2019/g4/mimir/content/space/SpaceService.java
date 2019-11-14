package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
     * @parm rootFolder
     * @param principal owner
     * @return
     */
    public Space create(String name, Folder rootFolder, Principal principal) {
        Space space = new Space();
        space.setName(name);
        space.setRootFolder(rootFolder);
        space.setOwner(principal.getName());
        return spaceRepository.save(space);
    }

    public void delete(Space space) {
        spaceRepository.delete(space);
    }


}
