package de.seprojekt.se2019.g4.mimir.security.registration;

import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

  private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);
  private MailService mailService;
  private UserService userService;

  public RegistrationController(MailService mailService, UserService userService) {
    this.mailService = mailService;
    this.userService = userService;
  }

  /**
   * registers a new user
   */
  @PostMapping(value = "/register")
  public ResponseEntity register(@RequestParam("mail") String mail,
      @RequestParam("password") String password) {
    if (StringUtils.isEmpty(mail) || StringUtils
        .isEmpty(password)) {
      return ResponseEntity.badRequest().build();
    }

    mail = mail.toLowerCase();

    if (!userService.isValidEmailAddress(mail)) {
      LOGGER.warn("Email " + mail + " has an invalid pattern");
      return ResponseEntity.badRequest().build();
    }
    if (!userService.isValidEmailDomain(mail)) {
      LOGGER.warn("Email " + mail + " has an invalid domain");
      return ResponseEntity.badRequest().build();
    }
    if (userService.findByMail(mail).isPresent()) {
      LOGGER.warn("User with mail " + mail + " already exists");
      return ResponseEntity.status(409).build();
    }

    // TODO send mail
    // mailService.sendMail(receiver, "Mimir-Testmail", text);

    userService.create(mail, password);
    return ResponseEntity.ok().build();
  }
}
