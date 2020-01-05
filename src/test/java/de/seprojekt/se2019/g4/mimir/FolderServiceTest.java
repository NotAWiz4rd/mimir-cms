package de.seprojekt.se2019.g4.mimir;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class FolderServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    FolderService folderService;

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
    public void shouldCreateFolder() {
        String folderName = "MyTestFolder";
        Folder folder = folderService.create(space.getRootFolder(), folderName);
        assertNotNull("should exist", folder);
        assertTrue("should be empty", folderService.isEmpty(folder));
        assertEquals("should have name", folderName, folder.getName());
        assertEquals("should have parent folder", space.getRootFolder(), folder.getParentFolder());
        assertEquals("should have space", space, folder.getSpace());
    }

    @Test
    public void shouldDeleteFolderAndChildren() throws IOException {
        String folderName = "MyTestFolder";
        String subFolderName = "MySubFolder";
        Folder folder = folderService.create(space.getRootFolder(), folderName);
        Folder subFolder = folderService.create(folder, subFolderName);
        Artifact artifact =  artifactService.create(
            "file1.txt",
            new MockMultipartFile("file1.txt", "file1.txt", "text/plain", "foobar".getBytes()),
            subFolder
        );
        folderService.delete(folder);
        assertTrue("folder should not exist", folderService.findById(folder.getId()).isEmpty());
        assertTrue("sub folder should not exist", folderService.findById(subFolder.getId()).isEmpty());
        assertTrue("artifact should not exist", artifactService.findById(artifact.getId()).isEmpty());
    }

    @Test
    public void shouldDownloadFolderAsZip() throws Exception {
        artifactService.create(
            "file1.txt",
            new MockMultipartFile("file1.txt", "file1.txt", "text/plain", "foobar".getBytes()),
            space.getRootFolder()
        );
        var zip = folderService.zip(space.getRootFolder());
        var in = new ZipInputStream(zip);
        var folder1 = in.getNextEntry();
        var file1 = in.getNextEntry();
        assertTrue("zip sollte folder1 enthalten", folder1.isDirectory());
        assertEquals("zip sollte file1 enthalten", space.getRootFolder().getName() + "/file1.txt", file1.getName());
        assertNotEquals("file1 sollte nicht leer sein", 0, file1.getSize());
    }

}