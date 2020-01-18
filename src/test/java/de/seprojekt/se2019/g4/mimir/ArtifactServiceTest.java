package de.seprojekt.se2019.g4.mimir;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.io.IOException;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ArtifactServiceTest {

  @Autowired
  UserService userService;

  @Autowired
  ArtifactService artifactService;

  private User user;
  private Space space;

  @Before
  public void init() {
    user = userService.findByName("thellmann").get();
    space = user.getSpaces().get(0);
  }

  @Test
  public void shouldCreateArtifact() throws IOException {
    String name = "file1.txt";
    Artifact artifact = artifactService.create(
        name,
        user.getName(),
        new MockMultipartFile(name, name, "text/plain", "foobar".getBytes()),
        space.getRootFolder()
    );
    assertNotNull("should exist", artifact);
    assertEquals("should have name", name, artifact.getName());
    assertEquals("should have author", user.getName(), artifact.getAuthor());
    assertEquals("should have parent folder", space.getRootFolder(), artifact.getParentFolder());
    assertEquals("should have space", space, artifact.getSpace());
    assertEquals("should have content type", MediaType.valueOf("text/plain"),
        artifact.getContentType());
  }

  @Test
  public void shouldDeleteArtifact() throws IOException {
    String name = "file1.txt";
    Artifact artifact = artifactService.create(
        name,
        user.getName(),
        new MockMultipartFile(name, name, "text/plain", "foobar".getBytes()),
        space.getRootFolder()
    );
    artifactService.delete(artifact);
    assertTrue("should not exist", artifactService.findById(artifact.getId()).isEmpty());
  }

}
