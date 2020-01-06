package de.seprojekt.se2019.g4.mimir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.security.JwtTokenProvider;
import de.seprojekt.se2019.g4.mimir.security.registration.RegistrationController;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class RegistrationControllerTest {

  @Autowired
  RegistrationController registrationController;

  @Autowired
  UserService userService;

  @Autowired
  JwtTokenProvider tokenProvider;

  @Value("${app.validMailDomain}")
  private String validMailDomain;

  @Test
  public void shouldSendRegistrationMail() {
    assertEquals("should check if mail is not empty", 400,
        registrationController.registrationMail("").getStatusCodeValue());
    assertEquals("should check if mail is valid", 400,
        registrationController.registrationMail("i@valid@mail.me").getStatusCodeValue());
    assertEquals("should check if mail is from valid domain", 400,
        registrationController.registrationMail("test123@not" + validMailDomain)
            .getStatusCodeValue());
    assertEquals("should try to send the mail and should fail because the mail server is offline",
        503,
        registrationController.registrationMail("test123@" + validMailDomain).getStatusCodeValue());
  }

  @Test
  public void shouldHandleRegistrationConfirmation() throws JsonProcessingException {
    String mail = "test123@" + validMailDomain;
    String tokenNewUser = tokenProvider.generateRegistrationToken(mail);
    String tokenExistingUser = tokenProvider.generateRegistrationToken("t.hellmann@ostfalia.de");
    String shareToken = extractTokenFromBody(
        tokenProvider.generateShareToken(-1L, Artifact.TYPE_IDENTIFIER, 50000L));
    assertEquals("should check if token is empty", 400,
        registrationController.registrationConfirmation("", "pw").getStatusCodeValue());
    assertEquals("should check if password is empty", 400,
        registrationController.registrationConfirmation("token", "").getStatusCodeValue());
    assertEquals("should check if token is for registration", 400,
        registrationController.registrationConfirmation(shareToken, "pw").getStatusCodeValue());
    assertEquals("should check if user with mail already exists", 409,
        registrationController.registrationConfirmation(tokenExistingUser, "pw")
            .getStatusCodeValue());

    registrationController.registrationConfirmation(tokenNewUser, "pw");
    Optional<User> userOptional = userService.findByMail(mail);

    assertTrue("user should exist", userOptional.isPresent());
    assertEquals("user should have correct mail", mail, userOptional.get().getMail());
  }

  private String extractTokenFromBody(String body) {
    String token = body.split(":")[1];
    token = token.replace("\"", "");
    token = token.replace("}", "");
    return token.trim();
  }
}
