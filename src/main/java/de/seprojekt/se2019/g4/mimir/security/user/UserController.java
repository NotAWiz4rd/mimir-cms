package de.seprojekt.se2019.g4.mimir.security.user;

import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

  private UserService userService;

  /**
   * The parameters will be autowired by Spring.
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Returns the user identified by the Principal, returns fake Guest user if Principal is
   * anonymous.
   */
  @GetMapping(value = "/user")
  public ResponseEntity<User> getUser(Principal principal) {
    if (JwtPrincipal.fromPrincipal(principal)
        .isAnonymous()) { // return fake Guest user for the Frontend ot use
      User anonymous = new User();
      anonymous.setName("Guest");
      anonymous.setId(-1L);
      return ResponseEntity.ok().body(new User());
    }
    return ResponseEntity.ok().body(userService.findByName(principal.getName()).get());
  }
}
