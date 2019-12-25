package de.seprojekt.se2019.g4.mimir.security.registration;

import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

  private MailService mailService;
  private UserService userService;

  public RegistrationController(MailService mailService, UserService userService) {
    this.mailService = mailService;
    this.userService = userService;
  }

  /**
   * registers a new user
   */
  @GetMapping(value = "/register")
  public ResponseEntity register(@RequestParam("mail") String mail, @RequestParam("password") String password)
      throws Exception {
    if (StringUtils.isEmpty(password) || StringUtils
        .isEmpty(mail)) {
      return ResponseEntity.badRequest().build();
    }

    // TODO send mail
    // mailService.sendMail(receiver, "Mimir-Testmail", text);

    if (userService.create(mail, password) != null) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }
}
