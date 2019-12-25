package de.seprojekt.se2019.g4.mimir.security.user;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import de.seprojekt.se2019.g4.mimir.security.LdapClient;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);
  private UserRepository userRepository;
  private FolderService folderService;
  private ArtifactService artifactService;
  private SpaceService spaceService;
  private LdapClient ldapClient;

  public UserService(UserRepository userRepository, @Lazy FolderService folderService,
      @Lazy ArtifactService artifactService, @Lazy SpaceService spaceService,
      LdapClient ldapClient) {
    this.userRepository = userRepository;
    this.folderService = folderService;
    this.artifactService = artifactService;
    this.spaceService = spaceService;
    this.ldapClient = ldapClient;
  }

  @Transactional
  public User create(String mail, String password) {
    if(!this.isValidEmailAddress(mail)) {
      LOGGER.warn("Email " + mail + " has an invalid pattern");
      return null;
    }

    String username = mail
        .toLowerCase()
        .split("@")[0]
        .replace(".", "");

    if (this.findByName(username).isPresent()) {
      LOGGER.warn("User " + username + " already exists");
      return null;
    }

    LOGGER.info("Creating new user in LDAP: " + username);
    User user = new User();
    user.setName(username);
    user.setMail(mail);
    user.setSpaces(new ArrayList<>());
    user = userRepository.save(user);

    spaceService.create(username, new JwtPrincipal(username));
    ldapClient.registerLdapUser(username, password);

    return user;
  }

  /**
   * Return user with given name
   */
  public Optional<User> findByName(String name) {
    return this.userRepository.findByName(name);
  }

  /**
   * Return user with given id
   */
  public Optional<User> findById(long userId) {
    return this.userRepository.findById(userId);
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
   * Gets Users By Space.
   *
   * @param space The space in question.
   * @return A list of Users of said Space.
   */
  @Transactional
  public List<User> getUsersBySpace(Space space) {
    return this.userRepository.findAllBySpacesContains(space);
  }

  /**
   * Adds user to this space
   */
  @Transactional
  public User addUserToSpace(User user, Space space) {
    user = this.findByName(user.getName()).get();
    List<Space> spaceList = user.getSpaces();
    if (!spaceList.contains(space)) {
      spaceList.add(space);
    }
    return this.update(user);
  }

  /**
   * Check if user is authorized for space
   */
  @Transactional
  public boolean isAuthorizedForSpace(Space space, Principal principal) {
    JwtPrincipal jwtPrincipal = JwtPrincipal.fromPrincipal(principal);
    if (jwtPrincipal.isAnonymous()) {
      return false; // spaces can't be shared
    }
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
    JwtPrincipal jwtPrincipal = JwtPrincipal.fromPrincipal(principal);
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
    JwtPrincipal jwtPrincipal = JwtPrincipal.fromPrincipal(principal);
    if (jwtPrincipal.isAnonymous()) {
      switch (jwtPrincipal.getSharedEntityType()) {
        case Artifact.TYPE_IDENTIFIER: {
          Optional<Artifact> sharedArtifact = artifactService
              .findById(jwtPrincipal.getSharedEntityId());
          if (sharedArtifact.isEmpty()) {
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

  /**
   * https://stackoverflow.com/questions/624581/
   * @param email
   * @return
   */
  public boolean isValidEmailAddress(String email) {
    String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
    Pattern p = java.util.regex.Pattern.compile(ePattern);
    Matcher m = p.matcher(email);
    return m.matches();
  }

}
