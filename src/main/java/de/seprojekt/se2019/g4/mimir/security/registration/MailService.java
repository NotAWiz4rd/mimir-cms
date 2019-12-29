package de.seprojekt.se2019.g4.mimir.security.registration;

import java.util.Date;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

  private JavaMailSender javaMailSender;

  public MailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  public void sendRegistrationMail(String to, String link, Date expiration) {
    String text =
          "Thank you for becoming a member!\n\n"
        + "Open the following link to complete your registration: " + link + "\n\n"
        + "(The link expires at " + expiration + ")";

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("cmsplusplus@ostfalia.de");
    message.setTo(to);
    message.setSubject("CMS++ Registration");
    message.setText(text);
    this.javaMailSender.send(message);
  }
}
