package de.seprojekt.se2019.g4.mimir.security.user;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);
  private UserRepository userRepository;
  private FolderService folderService;
  private ArtifactService artifactService;

  public UserService(UserRepository userRepository, FolderService folderService, ArtifactService artifactService) {
    this.userRepository = userRepository;
    this.folderService = folderService;
    this.artifactService = artifactService;
  }

  @Transactional
  public User create(String name, String mail) {
    User user = new User();
    user.setName(name);
    user.setMail(mail);
    user.setSpaces(new ArrayList<>());
    return userRepository.save(user);
  }

  /**
   * Return user with given name
   */
  public Optional<User> findByName(String name) {
    return this.userRepository.findByName(name);
  }


  /**
   * Update a user
   */
  @Transactional
  public User update(User user) {
    return userRepository.save(user);
  }

  /**
   * returns number of users for this space
   */
  @Transactional
  public Integer getNumberOfSpaceUsers(Space space) {
    return this.userRepository.countBySpacesContains(space);
  }

  /**
   * removes all users from this space
   */
  @Transactional
  public void removeAllFromSpace(Space space) {
    for (User user : this.userRepository.findAllBySpacesContains(space)) {
      user.getSpaces().remove(space);
      this.update(user);
    }
  }

  /**
   * Adds user to this space
   */
  @Transactional
  public User addUserToSpace(User user, Space space) {
    user = this.findByName(user.getName()).get();
    user.getSpaces().add(space);
    return this.update(user);
  }

  /**
   * Check if user is authorized for space
   */
  @Transactional
  public boolean isAuthorizedForSpace(Space space, Principal principal) {
    Optional<User> optionalUser = this.findByName(principal.getName());
    if (optionalUser.isEmpty()) {
      return false;
    }
    return optionalUser.get().getSpaces().contains(space);
  }

  /**
   * Check if user is authorized for folder
   */
  @Transactional
  public boolean isAuthorizedForFolder(Folder folder, Principal principal) {
    JwtPrincipal jwtPrincipal = (JwtPrincipal) principal;
    if (jwtPrincipal.isAnonymous()) {
      switch (jwtPrincipal.getSharedEntityType()) {
        case Artifact.TYPE_IDENTIFIER:
          return false;
        case Folder.TYPE_IDENTIFIER: {
          Optional<Folder> sharedFolder = folderService.findById(jwtPrincipal.getSharedEntityId());
          if (sharedFolder.isEmpty()) {
            return false;
          }
          return folderService.matchesOrIsChild(sharedFolder.get(), folder);
        }
        default:
          return false;
      }
    } else {
      return this.isAuthorizedForSpace(folder.getSpace(), principal);
    }
  }

  /**
   * Check if user is authorized for artifact
   */
  @Transactional
  public boolean isAuthorizedForArtifact(Artifact artifact, Principal principal) {
    JwtPrincipal jwtPrincipal = (JwtPrincipal) principal;
    if (jwtPrincipal.isAnonymous()) {
      switch (jwtPrincipal.getSharedEntityType()) {
        case Artifact.TYPE_IDENTIFIER: {
          Optional<Artifact> sharedArtifact = artifactService.findById(jwtPrincipal.getSharedEntityId());
          if(sharedArtifact.isEmpty()) {
            return false;
          }
          return sharedArtifact.get().getId() == artifact.getId();
        }
        case Folder.TYPE_IDENTIFIER: {
          Optional<Folder> sharedFolder = folderService.findById(jwtPrincipal.getSharedEntityId());
          if (sharedFolder.isEmpty()) {
            return false;
          }
          return folderService.matchesOrIsChild(sharedFolder.get(), artifact.getParentFolder());
        }
        default:
          return false;
      }
    } else {
      return this.isAuthorizedForSpace(artifact.getSpace(), principal);
    }
  }

}
