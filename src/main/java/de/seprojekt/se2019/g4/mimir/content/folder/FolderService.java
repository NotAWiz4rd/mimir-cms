package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.ContentService;
import de.seprojekt.se2019.g4.mimir.content.DisplayableException;
import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
    private ContentService contentService;

    /**
     * The parameters will be autowired by Spring.
     * @param folderRepository
     * @param artifactRepository
     * @param contentService
     */
    public FolderService(FolderRepository folderRepository, ArtifactRepository artifactRepository, ContentService contentService) {
        this.folderRepository = folderRepository;
        this.artifactRepository = artifactRepository;
        this.contentService = contentService;
    }

    /**
     * Return the folder with the given id.
     * @param id
     * @return
     */
    public Optional<Folder> findById(long id) {
        return folderRepository.findById(id);
    }

    /**
     * Return the folder with the given total url.
     * @param totalUrl
     * @return
     */
    public Optional<Folder> findByTotalUrl(String totalUrl) {
        return folderRepository.findByTotalUrl(totalUrl);
    }

    /**
     * Create a new folder and validate the parentUrl and nameWithoutEndingSlash.
     * This method will perform some checks (is name not dangerous? does a folder with the name already exists?).
     * @param parentUrl
     * @param nameWithoutEndSlash
     * @return
     */
    @Transactional
    public Folder create(String parentUrl, String nameWithoutEndSlash) {
        Assert.isTrue(parentUrl.endsWith("/"), "parentUrl muss mit einem Schrägstrich enden");
        Assert.isTrue(!nameWithoutEndSlash.startsWith("/"), "nameWithoutEndSlash must not start with an slash");
        Assert.isTrue(!nameWithoutEndSlash.endsWith("/"), "nameWithoutEndSlash darf keinen Schrägstrich haben");

        if (contentService.isNameDangerous(nameWithoutEndSlash)) {
            throw new DisplayableException( "Der Ordnername darf keine verbotene Zeichen enthalten.");
        }
        if (exists(parentUrl, nameWithoutEndSlash)) {
            throw new DisplayableException( "Ordner existiert bereits.");
        }
        String totalUrl = parentUrl + nameWithoutEndSlash + "/";
        if (totalUrl.length() > 300) {
            throw new DisplayableException("Der Name ist zu lang.");
        }

        Folder folder = new Folder();
        folder.setParentUrl(parentUrl);
        folder.setTotalUrl(totalUrl);
        folder.setDisplayName(nameWithoutEndSlash);
        return folderRepository.save(folder);
    }

    /**
     * Check if a folder with the given parentUrl and nameWithoutEndingSlash already exists
     * @param parentUrl
     * @param nameWithoutEndingSlash
     * @return
     */
    public boolean exists(String parentUrl, String nameWithoutEndingSlash) {
        Assert.isTrue(parentUrl.endsWith("/"), "parentUrl must end with an slash");
        Assert.isTrue(!nameWithoutEndingSlash.startsWith("/"), "name must not start with an slash");
        Assert.isTrue(!nameWithoutEndingSlash.endsWith("/"), "name must not end with an slash");
        return folderRepository.existsByTotalUrl(parentUrl + nameWithoutEndingSlash + "/");
    }

    /**
     * Check if an folder with the given totalUrl already exists
     * @param totalUrl
     * @return
     */
    public boolean exists(String totalUrl) {
        Assert.isTrue(totalUrl.endsWith("/"), "totalUrl must end with an slash");
        return folderRepository.existsByTotalUrl(totalUrl) || "/".equals(totalUrl);
    }

    /**
     * Check if an folder is empty (does not contain any artifacts or folders)
     * @param folder
     * @return
     */
    public boolean isEmpty(Folder folder) {
        String folderUrl = folder.getTotalUrl();
        // check for sub(sub...)folders amd sub(sub...)artifacts
        return !(artifactRepository.existsByParentUrlStartingWith(folderUrl) || folderRepository.existsByParentUrlStartingWith(folderUrl));
    }

    /**
     * Rename an already existing folder.
     * Please do not use the old folder object again - use the returned folder object.
     * This method will perform some checks (does a folder with the name already exists? is name not dangerous?).
     * @param folder
     * @param newNameWithoutEndingSlash
     * @return
     */
    @Transactional
    public Folder rename(Folder folder, String newNameWithoutEndingSlash) {
        Assert.isTrue(!newNameWithoutEndingSlash.isEmpty(), "newNameWithoutEndingSlash must not be empty");
        Assert.isTrue(!newNameWithoutEndingSlash.startsWith("/"), "newNameWithoutEndingSlash must not start with a slash");
        Assert.isTrue(!newNameWithoutEndingSlash.endsWith("/"), "newNameWithoutEndingSlash must not end with a slash");

        if (exists(folder.getParentUrl(), newNameWithoutEndingSlash)) {
            throw new DisplayableException( "Ein Ordner mit dem Namen existiert bereits.");
        }

        if (contentService.isNameDangerous(newNameWithoutEndingSlash)) {
            throw new DisplayableException( "Der Ordnername darf keine verbotene Zeichen enthalten.");
        }
        String totalUrl = folder.getParentUrl() + newNameWithoutEndingSlash + "/";
        if (totalUrl.length() > 300) {
            throw new DisplayableException("Der Name ist zu lang.");
        }

        String oldTotalUrl = folder.getTotalUrl();
        folder.setDisplayName(newNameWithoutEndingSlash);
        folder.setTotalUrl(totalUrl);
        Folder renamedFolder = folderRepository.save(folder);
        String newTotalUrl = renamedFolder.getTotalUrl();

        renameSubArtifacts(oldTotalUrl, newTotalUrl);
        renameSubFolders(oldTotalUrl, newTotalUrl);
        return renamedFolder;
    }

    /**
     * This is a helper function for renaming folder. Because the folder and all its content like artifacts
     * must also be renamed (because parentUrl and totalUrl will be changed)
     * @param oldTotalUrl
     * @param newTotalurl
     */
    private void renameSubArtifacts(String oldTotalUrl, String newTotalurl) {
        List<Artifact> artifacts = artifactRepository.findByParentUrlStartingWith(oldTotalUrl);
        for (Artifact artifact : artifacts) {
            artifact.setParentUrl(artifact.getParentUrl().replace(oldTotalUrl, newTotalurl));
            artifact.setTotalUrl(artifact.getTotalUrl().replace(oldTotalUrl, newTotalurl));
        }
        artifactRepository.saveAll(artifacts);
    }

    /**
     * This is a helper function for renaming folder. Because the folder and all its content like folders
     * must also be renamed (because parentUrl and totalUrl will be changed)
     * @param oldTotalUrl
     * @param newTotalurl
     */
    private void renameSubFolders(String oldTotalUrl, String newTotalurl) {
        List<Folder> folders = folderRepository.findByParentUrlStartingWith(oldTotalUrl);
        for (Folder folder : folders) {
            folder.setParentUrl(folder.getParentUrl().replace(oldTotalUrl, newTotalurl));
            folder.setTotalUrl(folder.getTotalUrl().replace(oldTotalUrl, newTotalurl));
        }
        folderRepository.saveAll(folders);
    }

    /**
     * Delete the given folder.
     *
     * This method will perform some checks (is the folder empty?).
     * @param folder
     */
    @Transactional
    public void delete(Folder folder) {
        if (!isEmpty(folder)) {
            throw new DisplayableException( "Der Ordner kann nicht gelöscht werden, weil er nicht leer ist.");
        }
        folderRepository.delete(folder);
    }
}
