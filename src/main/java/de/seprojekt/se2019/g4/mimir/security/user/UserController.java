package de.seprojekt.se2019.g4.mimir.security.user;

import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import java.security.Principal;
import java.util.Optional;
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
    JwtPrincipal jwtPrincipal = JwtPrincipal.fromPrincipal(principal);

    if (jwtPrincipal.isAnonymous()) {// return fake Guest user for the Frontend ot use
      User anonymous = new User();
      anonymous.setName("Guest");
      anonymous.setId(-1L);
      return ResponseEntity.ok().body(anonymous);
    }

    Optional<User> userOptional = userService.findByName(jwtPrincipal.getName());
    if (userOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok().body(userOptional.get());
  }
}
