package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import java.util.List;

/**
 * This class acts as a data transfer object for folders to add the list of child folders and child
 * artifacts
 */
public class FolderDTO extends Folder {

  private List<FolderDTO> folders;
  private List<Artifact> artifacts;

  public FolderDTO(Folder folder) {
    this.setId(folder.getId());
    this.setName(folder.getName());
    this.setParentFolder(folder.getParentFolder());
  }

  public List<FolderDTO> getFolders() {
    return folders;
  }

  public void setFolders(List<FolderDTO> folders) {
    this.folders = folders;
  }

  public List<Artifact> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<Artifact> artifacts) {
    this.artifacts = artifacts;
  }
}
