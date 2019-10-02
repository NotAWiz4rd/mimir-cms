package de.seprojekt.se2019.g4.mimir.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This controller will display the template "login" for a HTTP request for the url "/login"
 */
@Controller
public class LoginController {
    private final static Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     * @param applicationBaseUrl
     */
    public LoginController(@Value("${application.base.url}") String applicationBaseUrl) {
        this.applicationBaseUrl = applicationBaseUrl;
    }

    /**
     * Display the login page.
     * @param model
     * @return
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("applicationBaseUrl", applicationBaseUrl);
        return "login";
    }
}
