package de.seprojekt.se2019.g4.mimir.security.registration;

import de.seprojekt.se2019.g4.mimir.security.JwtTokenProvider;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
  private JwtTokenProvider jwtTokenProvider;

  @Value("${app.frontendRegistrationUrl}")
  private String frontendRegistrationUrl;

  public RegistrationController(MailService mailService, UserService userService, JwtTokenProvider jwtTokenProvider) {
    this.mailService = mailService;
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  /**
   * starts registration of a user by sending a mail to the provided email address
   */
  @PostMapping(value = "/register/mail")
  public ResponseEntity registrationMail(@RequestParam("mail") String mail) {
    if (StringUtils.isEmpty(mail)) {
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

    String token = jwtTokenProvider.generateRegistrationToken(mail);
    String link = this.frontendRegistrationUrl + "?mail=" + mail + "&token=" + token;
    mailService.sendRegistrationMail(mail, link, jwtTokenProvider.getExpiration(token));

    return ResponseEntity.ok().build();
  }

  /**
   * finishes registration of a user by creating a new ldap entry
   */
  @PostMapping(value = "/register/confirm")
  public ResponseEntity registrationConfirmation(@RequestParam("token") String token,
      @RequestParam("password") String password) {
    if (StringUtils.isEmpty(token) || StringUtils.isEmpty(password)) {
      return ResponseEntity.badRequest().build();
    }

    if (!jwtTokenProvider.validateToken(token)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    if (!jwtTokenProvider.getPayload(token, "type").equals(User.REGISTRATION_IDENTIFIER)) {
      return ResponseEntity.badRequest().build();
    }

    String mail = jwtTokenProvider.getPayload(token, "sub");

    if (StringUtils.isEmpty(mail)) {
      return ResponseEntity.badRequest().build();
    }

    userService.create(mail, password);
    return ResponseEntity.ok().build();
  }
}
