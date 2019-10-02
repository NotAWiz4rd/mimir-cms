package de.seprojekt.se2019.g4.mimir;

import de.seprojekt.se2019.g4.mimir.content.ContentService;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * This class will be automatically executed on application startup, when then current profile != test is.
 *
 */
@Service
@Profile("!test")
@ConditionalOnProperty(name = "application.generate.example-data", havingValue = "true")
public class ExampleDataGenerator implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDataGenerator.class);
    private ArtifactService artifactService;
    private FolderService folderService;
    private Principal principal;
    private ContentService contentService;

    public ExampleDataGenerator(ArtifactService artifactService, FolderService folderService, ContentService contentService) {
        this.artifactService = artifactService;
        this.folderService = folderService;
        this.contentService = contentService;
        this.principal = () -> "thellmann";
    }

    /**
     * When this CommandLineRunner is executed, folders will be created and artifacts will be added.
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        int numberOfElements = contentService.findContentForFolder("/").map(List::size).orElse(0);
        if (numberOfElements > 0) {
            LOGGER.info("Database is not empty - abort generation of example data");
            return;
        }

        Folder folder1 = folderService.create("/", "Aufgabe 📬");

        uploadFile("/", "Innenhof", MediaType.IMAGE_JPEG, "example_data/innenhof.jpg");
        uploadFile(folder1.getTotalUrl(), "SE-Projekt Aufgabe", MediaType.TEXT_HTML, "example_data/aufgabenstellung.html");
        uploadFile("/", "Beispielvideo Final1", MediaType.valueOf("video/mp4"), "example_data/SampleVideo_1280x720_5mb.mp4");
    }

    /**
     * source: https://stackoverflow.com/a/20572072
     * @param systemPath
     * @param name
     * @param mediaType
     * @param parentUrl
     * @throws IOException
     */
    private void uploadFile(String parentUrl, String name, MediaType mediaType, String systemPath) throws IOException {
        MultipartFile multipartFile = new ExampleMultipartFile(name, mediaType, new ClassPathResource(systemPath));
        artifactService.initialCheckin(name, multipartFile, parentUrl, principal);
        LOGGER.info("Added artifact '{}'", name);
    }
}