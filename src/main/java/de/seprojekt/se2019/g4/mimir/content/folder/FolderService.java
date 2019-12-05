package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This service offers helper methods for dealing with folders.
 */
@Service
public class FolderService {
    private final static Logger LOGGER = LoggerFactory.getLogger(FolderService.class);
    private FolderRepository folderRepository;
    private ArtifactService artifactService;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param folderRepository
     * @param artifactService
     */
    public FolderService(FolderRepository folderRepository, ArtifactService artifactService) {
        this.folderRepository = folderRepository;
        this.artifactService = artifactService;
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
     * Return list of folder with given parent folder
     *
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
     * @param name
     * @return
     */
    @Transactional
    public Folder create(Folder parentFolder, String name) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setParentFolder(parentFolder);
        return folderRepository.save(folder);
    }

    /**
     * Update a folder
     *
     * @param folder
     * @return
     */
    @Transactional
    public Folder update(Folder folder) {
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
     * Returns a folder DTO that contains the folder tree
     *
     * @param folder
     * @return
     */
    public FolderDTO getFolderDTOWithTree(Folder folder) {
        FolderDTO folderDTO = new FolderDTO(folder);
        folderDTO.setFolders(this.getFolderTree(folder));
        folderDTO.setArtifacts(artifactService.findByParentFolder(folder));
        return folderDTO;
    }

    /**
     * Recursive method to create a tree of folders
     *
     * @param folder
     * @return
     */
    public List<FolderDTO> getFolderTree(Folder folder) {
        List<Folder> childFolders = folderRepository.findByParentFolder(folder);
        List<FolderDTO> folderDTOs = new LinkedList<>();
        if (childFolders.size() == 0) {
            FolderDTO folderDTO = new FolderDTO(folder);
            folderDTO.setArtifacts(this.artifactService.findByParentFolder(folder));
            return folderDTOs;
        } else {
            childFolders.forEach(f -> {
                FolderDTO folderDTO = new FolderDTO(f);
                folderDTO.setFolders(this.getFolderTree(f));
                folderDTO.setArtifacts(this.artifactService.findByParentFolder(f));
                folderDTOs.add(folderDTO);
            });
            return folderDTOs;
        }
    }

    /**
     * ZIPs a folder and its content
     * https://www.baeldung.com/java-compress-and-uncompress
     *
     * @param folder
     * @throws IOException
     */
    public ByteArrayInputStream zip(Folder folder) throws IOException {
        // create ZipOutputStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(bos);

        // add root folder
        zipOutputStream.putNextEntry(new ZipEntry(folder.getName() + "/"));
        zipOutputStream.closeEntry();

        // add artifacts of root folder
        this.zipFolderArtifacts(folder, folder.getName(), zipOutputStream);

        // recursively add all children folders with artifacts
        for (Folder childFolder : folderRepository.findByParentFolder(folder)) {
            this.zipFolder(childFolder, folder.getName() + "/" + childFolder.getName(), zipOutputStream);
        }

        zipOutputStream.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    /**
     * Recursive method to zip folders
     *
     * @param folder
     * @param path
     * @param zipOutputStream
     * @throws IOException
     */
    private void zipFolder(Folder folder, String path, ZipOutputStream zipOutputStream) throws IOException {
        List<Folder> childFolders = folderRepository.findByParentFolder(folder);

        // if no further childFolders exist, then end recursion
        if (childFolders.size() == 0) {
            zipOutputStream.putNextEntry(new ZipEntry(path + "/"));
            zipOutputStream.closeEntry();
            this.zipFolderArtifacts(folder, path, zipOutputStream);
            return;
        }

        // otherwise zip childFolders and their artifacts as well
        zipOutputStream.putNextEntry(new ZipEntry(path + "/"));
        zipOutputStream.closeEntry();
        this.zipFolderArtifacts(folder, path, zipOutputStream);
        for (Folder childFolder : childFolders) {
            zipFolder(childFolder, path + "/" + childFolder.getName(), zipOutputStream);
        }
        return;
    }

    /**
     * ZIPs artifacts of a specific folder
     *
     * @param folder
     * @param path
     * @param zipOutputStream
     * @throws IOException
     */
    private void zipFolderArtifacts(Folder folder, String path, ZipOutputStream zipOutputStream) throws IOException {
        for (Artifact childArtifact : this.artifactService.findByParentFolder(folder)) {
            zipOutputStream.putNextEntry(new ZipEntry(path + "/" + artifactService.calculateFileName(childArtifact)));
            zipOutputStream.write(childArtifact.getContent());
            zipOutputStream.closeEntry();
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
        return !(artifactService.existsByParentFolder(folder) || folderRepository.existsByParentFolder(folder));
    }

    /**
     * Deletes a folder, its sub-folders and all artifacts
     *
     * @param folder
     */
    @Transactional
    public void delete(Folder folder) {
        List<Folder> childFolders = folderRepository.findByParentFolder(folder);
        for (Folder f: childFolders){
          this.delete(f);
        }
        for (Artifact artifact : artifactService.findByParentFolder(folder)) {
          artifactService.delete(artifact);
        }
        folderRepository.delete(folder);
    }
}
