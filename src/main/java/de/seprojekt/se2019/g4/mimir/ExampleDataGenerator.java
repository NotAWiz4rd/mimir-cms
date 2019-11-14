package de.seprojekt.se2019.g4.mimir;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
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

/**
 * This class will be automatically executed on application startup, when then current profile != test is.
 */
@Service
@Profile("!test")
@ConditionalOnProperty(name = "application.generate.example-data", havingValue = "true")
public class ExampleDataGenerator implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDataGenerator.class);
    private ArtifactService artifactService;
    private FolderService folderService;
    private SpaceService spaceService;
    private Principal principal;

    public ExampleDataGenerator(ArtifactService artifactService, FolderService folderService, SpaceService spaceService) {
        this.artifactService = artifactService;
        this.folderService = folderService;
        this.spaceService = spaceService;
        this.principal = () -> "GENERATOR-USER";
    }

    /**
     * When this CommandLineRunner is executed, folders will be created and artifacts will be added.
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        Folder root = folderService.create(null, "space-1");
        Space space = spaceService.create("space-1", root, principal);

        Folder task = folderService.create(folderService.findByParentFolderAndDisplayName(null, "space-1").get(), "Aufgabe 📬");

        uploadFile(root, "Innenhof", MediaType.IMAGE_JPEG, "example_data/innenhof.jpg");
        uploadFile(task, "SE-Projekt Aufgabe", MediaType.TEXT_HTML, "example_data/aufgabenstellung.html");
        uploadFile(root, "Beispielvideo Final1", MediaType.valueOf("video/mp4"), "example_data/SampleVideo_1280x720_5mb.mp4");
    }

    /**
     * source: https://stackoverflow.com/a/20572072
     *
     * @param systemPath
     * @param name
     * @param mediaType
     * @param parentFolder
     * @throws IOException
     */
    private void uploadFile(Folder parentFolder, String name, MediaType mediaType, String systemPath) throws IOException {
        MultipartFile multipartFile = new ExampleMultipartFile(name, mediaType, new ClassPathResource(systemPath));
        artifactService.upload(name, multipartFile, parentFolder, principal);
        LOGGER.info("Added artifact '{}'", name);
    }
}