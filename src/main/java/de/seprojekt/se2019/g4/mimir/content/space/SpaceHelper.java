package de.seprojekt.se2019.g4.mimir.content.space;

import de.seprojekt.se2019.g4.mimir.content.folder.FolderHelper;

public class SpaceHelper extends Space{

    public SpaceHelper(Space space) {
        this.setId(space.getId());
        this.setName(space.getName());
        this.setOwner(space.getOwner());
        this.setRootFolder(space.getRootFolder());
    }

    private FolderHelper root;

    public FolderHelper getRoot() {
        return root;
    }

    public void setRoot(FolderHelper root) {
        this.root = root;
    }
}