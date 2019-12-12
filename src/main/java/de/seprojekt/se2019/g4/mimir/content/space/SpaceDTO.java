package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.FolderDTO;

/**
 * This class acts as a data transfer object for space to add the tree of folders and artifacts
 */
public class SpaceDTO extends Space {

    private FolderDTO root;

    public SpaceDTO(Space space) {
        this.setId(space.getId());
        this.setName(space.getName());
        this.setRootFolder(space.getRootFolder());
    }

    public FolderDTO getRoot() {
        return root;
    }

    public void setRoot(FolderDTO root) {
        this.root = root;
    }
}