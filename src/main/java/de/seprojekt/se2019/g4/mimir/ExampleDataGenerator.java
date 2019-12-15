package de.seprojekt.se2019.g4.mimir;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class will be automatically executed on application startup, when then current profile !=
 * test is.
 */
@Service
@Profile("!test")
@ConditionalOnProperty(name = "application.generate.example-data", havingValue = "true")
public class ExampleDataGenerator implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDataGenerator.class);
  private ArtifactService artifactService;
  private FolderService folderService;
  private SpaceService spaceService;
  private UserService userService;

  public ExampleDataGenerator(ArtifactService artifactService, FolderService folderService,
      SpaceService spaceService, UserService userService) {
    this.artifactService = artifactService;
    this.folderService = folderService;
    this.spaceService = spaceService;
    this.userService = userService;
  }

  /**
   * When this CommandLineRunner is executed, folders will be created and artifacts will be added.
   */
  @Override
  public void run(String... args) throws Exception {
    User user1 = userService.create("thellmann", "t.hellmann@ostfalia.de");
    User user2 = userService.create("jbark", "jo.bark@ostfalia.de");

    Space space = spaceService.create("thellmann", () -> "thellmann");
    Space space2 = spaceService.create("jbark", () -> "jbark");

    Space sharedSpace = spaceService.create("shared", () -> "thellmann");
    userService.addUserToSpace(user2, sharedSpace);

    Folder sharedRoot = folderService.findByParentFolderAndDisplayName(null, "shared").get();
    Folder task = folderService.create(sharedRoot, "Aufgabe 📬");

    uploadFile(sharedRoot, "Innenhof.jpg", MediaType.IMAGE_JPEG, "example_data/innenhof.jpg");
    uploadFile(task, "SE-Projekt Aufgabe.html", MediaType.TEXT_HTML,
        "example_data/aufgabenstellung.html");
    uploadFile(sharedRoot, "Beispielvideo Final1.mp4", MediaType.valueOf("video/mp4"),
        "example_data/SampleVideo_1280x720_5mb.mp4");
  }

  /**
   * source: https://stackoverflow.com/a/20572072
   */
  private void uploadFile(Folder parentFolder, String name, MediaType mediaType, String systemPath)
      throws IOException {
    MultipartFile multipartFile = new ExampleMultipartFile(name, mediaType,
        new ClassPathResource(systemPath));
    artifactService.upload(name, multipartFile, parentFolder);
    LOGGER.info("Added artifact '{}'", name);
  }
}