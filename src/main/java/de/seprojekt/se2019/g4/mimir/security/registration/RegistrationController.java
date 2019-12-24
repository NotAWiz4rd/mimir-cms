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
  public ResponseEntity register(@RequestParam("username") String username,
      @RequestParam("password") String password, @RequestParam("mail") String mail)
      throws Exception {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils
        .isEmpty(mail)) {
      return ResponseEntity.badRequest().build();
    }
    // TODO send mail
    //mailService.sendMail(receiver, "Mimir-Testmail", text);
    // TODO check if user already exists
    userService.create(username, password, mail);
    return ResponseEntity.ok().build();
  }
}
