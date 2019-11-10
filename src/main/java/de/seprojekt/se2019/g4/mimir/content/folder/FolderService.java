package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.Content;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This service offers helper methods for dealing with folders.
 */
@Service
public class FolderService {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderService.class);
    private FolderRepository folderRepository;
    private ArtifactRepository artifactRepository;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param folderRepository
     * @param artifactRepository
     */
    public FolderService(FolderRepository folderRepository, ArtifactRepository artifactRepository) {
        this.folderRepository = folderRepository;
        this.artifactRepository = artifactRepository;
    }

    /**
     * Return the folder with the given id.
     *
     * @param id
     * @return
     */
    public Optional<Folder> findById(long id) {
        return folderRepository.findById(id);
    }

    /**
     * Return the folder optional with the given parent and name
     *
     * @param parentFolder
     * @param name
     * @return
     */
    public Optional<Folder> findByParentFolderAndDisplayName(Folder parentFolder, String name) {
        return folderRepository.findByParentFolderAndName(parentFolder, name);
    }

    /**
     * Return a list of root folders
     * @return
     */
    public List<Folder> findRootFolder() {
        return folderRepository.findByParentFolder(null);
    }

    /**
     * Return list of folder with given parent folder
     * @param parentFolder
     * @return
     */
    public List<Folder> findByParentFolder(Folder parentFolder) {
        return folderRepository.findByParentFolder(parentFolder);
    }

    /**
     * Create a new folder in the given parent folder with a given name
     *
     * @param parentFolder
     * @param displayName
     * @return
     */
    @Transactional
    public Folder create(Folder parentFolder, String displayName) {
        Folder folder = new Folder();
        folder.setName(displayName);
        folder.setParentFolder(parentFolder);
        return folderRepository.save(folder);

    }

    /**
     * Check if a folder in the given parent folder with the given name already exists
     *
     * @param parentFolder
     * @param name
     * @return
     */
    public boolean exists(Folder parentFolder, String name) {
        if (parentFolder == null) { // is root folder
            return folderRepository.existsByName(name);
        } else {
            return folderRepository.existsByParentFolderAndName(parentFolder, name);
        }
    }

    /**
     * Check if an folder is empty (does not contain any artifacts or folders)
     *
     * @param folder
     * @return
     */
    public boolean isEmpty(Folder folder) {
        // check for sub(sub...)folders amd sub(sub...)artifacts
        return !(artifactRepository.existsByParentFolder(folder) || folderRepository.existsByParentFolder(folder));
    }


    @Transactional
    public void delete(Folder folder) {
        folderRepository.delete(folder);
    }
}
