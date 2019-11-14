package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;

import java.util.List;

/**
 * This class wraps the folder class to add the list of child folders and child artifacts
 */
public class FolderHelper extends Folder {

    public FolderHelper(Folder folder) {
        this.setId(folder.getId());
        this.setName(folder.getName());
        this.setParentFolder(folder.getParentFolder());
    }

    private List<FolderHelper> folders;

    private List<Artifact> artifacts;

    public List<FolderHelper> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderHelper> folders) {
        this.folders = folders;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }
}
