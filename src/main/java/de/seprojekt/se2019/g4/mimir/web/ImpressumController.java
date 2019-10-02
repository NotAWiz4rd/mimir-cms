package de.seprojekt.se2019.g4.mimir.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImpressumController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImpressumController.class);
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     * @param applicationBaseUrl
     */
    public ImpressumController(@Value("${application.base.url}") String applicationBaseUrl) {
        this.applicationBaseUrl = applicationBaseUrl;
    }

    /**
     * Display the impressum page.
     * @param model
     * @return
     */
    @GetMapping(value = "/impressum")
    public String impressum(Model model) {
        model.addAttribute("applicationBaseUrl", applicationBaseUrl);
        return "impressum";
    }
}
