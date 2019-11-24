package de.seprojekt.se2019.g4.mimir.security.registration;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
	message.setFrom("mimir-cms@ostfalia.de");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        this.javaMailSender.send(message);
    }
}
