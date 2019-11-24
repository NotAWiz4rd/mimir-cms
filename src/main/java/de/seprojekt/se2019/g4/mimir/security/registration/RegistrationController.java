package de.seprojekt.se2019.g4.mimir.security.registration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private MailService mailService;

    public RegistrationController(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Sends a mail
     * @param receiver
     * @param text
     * @return
     */
    @GetMapping(value = "/login")
    public ResponseEntity getArtifact(@RequestParam("receiver") String receiver, @RequestParam("text") String text ) {
        if (StringUtils.isEmpty(receiver)) {
            return ResponseEntity.badRequest().build();
        }
        mailService.sendMail(receiver, "Mimir-Testmail", text);
        return ResponseEntity.ok().build();
    }
}
