package de.seprojekt.se2019.g4.mimir.content.icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.EntityNotFoundException;
import java.util.concurrent.TimeUnit;

/**
 * This controller offers an HTTP interface for accessing icons
 */
@Controller
public class IconDiscoveryWebController {
    private final static Logger LOGGER = LoggerFactory.getLogger(IconDiscoveryWebController.class);
    private IconDiscoverService iconDiscoverService;

    /**
     * The parameter will be autowired by Spring.
     * @param iconDiscoverService
     */
    public IconDiscoveryWebController(IconDiscoverService iconDiscoverService) {
        this.iconDiscoverService = iconDiscoverService;
    }

    /**
     * The user can access the icon for a given mimeType by calling this interface.
     * @param mimeType
     * @return
     */
    @GetMapping(value = "/icon/{mimeType:[0-9a-zA-Z+\\-.]+}")
    public ResponseEntity<ClassPathResource> icon(@PathVariable("mimeType") String mimeType) {
        ClassPathResource resource = iconDiscoverService.loadIconFor(mimeType).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(resource);

    }
}
