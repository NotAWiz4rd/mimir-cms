package de.seprojekt.se2019.g4.mimir.security.user;

import de.seprojekt.se2019.g4.mimir.content.space.Space;
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

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
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
   *
   * @param name
   * @return
   */
  public Optional<User> findByName(String name) {
    return this.userRepository.findByName(name);
  }


  /**
   * Update a user
   *
   * @param user
   * @return
   */
  @Transactional
  public User update(User user) {
    return userRepository.save(user);
  }

  /**
   * returns number of users for this space
   *
   * @param space
   * @return
   */
  @Transactional
  public Integer getNumberOfSpaceUsers(Space space) {
    return this.userRepository.countBySpacesContains(space);
  }

  /**
   * removes all users from this space
   *
   * @param space
   * @return
   */
  @Transactional
  public void removeAllFromSpace(Space space) {
    for(User user: this.userRepository.findAllBySpacesContains(space)) {
      user.getSpaces().remove(space);
      this.update(user);
    }
  }

  /**
   * Adds user to this space
   *
   * @param user
   * @param space
   * @return
   */
  @Transactional
  public User addUserToSpace(User user, Space space) {
    user = this.findByName(user.getName()).get();
    user.getSpaces().add(space);
    return this.update(user);
  }

  /**
   * Check if user is authorized for space
   *
   * @param space
   * @param principal
   * @return
   */
  @Transactional
  public boolean isAuthorizedForSpace(Space space, Principal principal) {
    Optional<User> optionalUser = this.findByName(principal.getName());
    if(optionalUser.isEmpty()) {
      return false;
    }
    return optionalUser.get().getSpaces().contains(space);
  }

}
