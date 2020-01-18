package de.seprojekt.se2019.g4.mimir;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.content.space.SpaceService;
import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class SpaceServiceTest {

  @Autowired
  UserService userService;

  @Autowired
  FolderService folderService;

  @Autowired
  SpaceService spaceService;

  @Test
  public void shouldCreateSpace() {
    String spaceName = "MyTestSpace";
    Space newSpace = spaceService.create(spaceName, new JwtPrincipal("thellmann"));
    Folder rootFolder = newSpace.getRootFolder();
    User user = userService.findByName("thellmann").get();
    assertNotNull("should exist", newSpace);
    assertNotNull("should have root folder", rootFolder);
    assertEquals("should have name", spaceName, newSpace.getName());
    assertTrue("user should be member of space", user.getSpaces().contains(newSpace));
  }

  @Test
  public void shouldDeleteSpace() {
    String spaceName = "MyTestSpace";
    Space newSpace = spaceService.create(spaceName, new JwtPrincipal("thellmann"));
    Folder rootFolder = newSpace.getRootFolder();
    Folder testFolder = folderService.create(newSpace.getRootFolder(), "spaceTestFolder");

    spaceService.delete(newSpace);
    User user = userService.findByName("thellmann").get();

    assertTrue("should not exist", spaceService.findById(newSpace.getId()).isEmpty());
    assertTrue("root folder should not exist",
        folderService.findById(rootFolder.getId()).isEmpty());
    assertTrue("child folder should not exist",
        folderService.findById(testFolder.getId()).isEmpty());
    assertFalse("user should not be member of space", user.getSpaces().contains(newSpace));
  }


}