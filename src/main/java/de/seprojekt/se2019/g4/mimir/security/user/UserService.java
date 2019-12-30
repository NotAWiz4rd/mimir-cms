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
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${app.validMailDomain}")
  private String validMailDomain;

  /**
   * The parameters will be autowired by Spring.
   */
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
    mail = mail.toLowerCase();

    if (!this.isValidEmailAddress(mail) ||
        this.findByMail(mail).isPresent()) {
      return null;
    }

    String username = mail
        .split("@")[0]
        .replace(".", "");

    if (this.findByName(username).isPresent()) {
      return null;
    }

    LOGGER.info("Creating user '{}' in DB", username);
    User user = new User();
    user.setName(username);
    user.setMail(mail);
    user.setSpaces(new ArrayList<>());
    user = userRepository.save(user);

    spaceService.create(username, new JwtPrincipal(username));

    LOGGER.info("Creating user '{}' in LDAP", username);
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
   * Return user with given mail
   */
  public Optional<User> findByMail(String mail) {
    return this.userRepository.findByMail(mail.toLowerCase());
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
      LOGGER.info("Adding user '{}' to space '{}'", user.getName(), space.getName());
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
      LOGGER.warn("Anonymous user tried accessing the space '{}'", space.getName());
      return false; // spaces can't be shared
    }
    Optional<User> optionalUser = this.findByName(principal.getName());
    if (optionalUser.isEmpty()) {
      return false;
    }
    if (optionalUser.get().getSpaces().contains(space)) {
      return true;
    } else {
      LOGGER.warn("User '{}' tried accessing the space '{}'", optionalUser.get().getName(),
          space.getName());
      return false;
    }
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
          LOGGER.warn(
              "Anonymous user tried accessing the folder '{}' with a share token for an artifact",
              folder.getName());
          return false;
        case Folder.TYPE_IDENTIFIER: {
          Optional<Folder> sharedFolder = folderService.findById(jwtPrincipal.getSharedEntityId());
          if (sharedFolder.isEmpty()) {
            return false;
          }
          if (folderService.matchesOrIsChild(sharedFolder.get(), folder)) {
            return true;
          } else {
            LOGGER.warn(
                "Anonymous user tried accessing the folder '{}' with a share token for the folder '{}",
                folder.getName(), sharedFolder.get().getName());
            return false;
          }
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
          if (sharedArtifact.get().getId() == artifact.getId()) {
            return true;
          } else {
            LOGGER.warn(
                "Anonymous user tried accessing the artifact '{}' with a share token for the artifact '{}",
                artifact.getName(), sharedArtifact.get().getName());
            return false;
          }
        }
        case Folder.TYPE_IDENTIFIER: {
          Optional<Folder> sharedFolder = folderService.findById(jwtPrincipal.getSharedEntityId());
          if (sharedFolder.isEmpty()) {
            return false;
          }
          if (folderService.matchesOrIsChild(sharedFolder.get(), artifact.getParentFolder())) {
            return true;
          } else {
            LOGGER.warn(
                "Anonymous user tried accessing the artifact '{}' with a share token for the folder '{}",
                artifact.getName(), sharedFolder.get().getName());
            return false;
          }
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
   */
  public boolean isValidEmailAddress(String email) {
    String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
    Pattern p = java.util.regex.Pattern.compile(ePattern);
    Matcher m = p.matcher(email);
    return m.matches();
  }

  /**
   * checks if mail address is from a valid domain
   */
  public boolean isValidEmailDomain(String email) {
    return email.endsWith("@" + this.validMailDomain);
  }

}
