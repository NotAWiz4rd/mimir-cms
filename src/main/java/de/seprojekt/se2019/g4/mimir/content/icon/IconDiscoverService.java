package de.seprojekt.se2019.g4.mimir.content.icon;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This service offers helper methods for dealing with icons.
 */
@Service
public class IconDiscoverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IconDiscoverService.class);

    /**
     * Return the icon for the given mediaType as an optional containing the ClassPathResource.
     *
     * @param mediaType
     * @return
     */
    public Optional<ClassPathResource> loadIconFor(MediaType mediaType) {
        String iconName = mediaType.toString().replace("/", "-");
        return loadIconFor(iconName);
    }

    /**
     * Return the icon for the given mimeType as an optional containing the ClassPathResource. The mimeType must
     * not contain forward slashes ('/'). All forward slashes must be replaces with a minus ('-')
     *
     * @param mimeType
     * @return
     */
    public Optional<ClassPathResource> loadIconFor(String mimeType) {
        return tryToLoad(mimeType + ".svg", 0).or(() -> tryToLoad("unknown.svg", 0));
    }

    /**
     * Try to load the icon from file system.
     * The difficulty: Either the file contains the icon or it contains the path to an other file.
     *
     * @param name
     * @param tries prevents an endless loop
     * @return
     */
    private Optional<ClassPathResource> tryToLoad(String name, int tries) {
        Assert.isTrue(tries < 10, "too many tries");

        String path = "icons/breeze-icons-dark/mimetypes/64/" + name;
        ClassPathResource resource = new ClassPathResource(path);
        String content;
        try (InputStream inputStream = resource.getInputStream()) {
            content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("cant load icon", e);
            return Optional.empty();
        }

        if (content.startsWith("<")) {
            return Optional.of(resource);
        }
        return tryToLoad(content, tries + 1);
    }
}
