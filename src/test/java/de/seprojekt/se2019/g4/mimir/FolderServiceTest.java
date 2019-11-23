package de.seprojekt.se2019.g4.mimir;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.security.Principal;
import java.util.zip.ZipInputStream;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class FolderServiceTest {
    @Autowired
    FolderService folderService;

    @Autowired
    ArtifactService artifactService;

    @Test
    public void shouldDownloadFolderAsZip() throws Exception {
        var parentFolder = folderService.create(null, "folder1");
        artifactService.upload(
            "file1",
            new MockMultipartFile("file1.txt", "file1.txt", "text/plain", "foobar".getBytes()),
            parentFolder,
            new Principal(){
                @Override public String getName() { return "testuser"; }
            });

        var zip = folderService.zip(parentFolder);
        var in = new ZipInputStream(zip);
        var folder1 = in.getNextEntry();
        var file1 = in.getNextEntry();
        assertTrue("zip sollte folder1 enthalten", folder1.isDirectory());
        assertEquals("zip sollte file1 enthalten", "folder1/file1.txt", file1.getName());
        assertNotEquals("file1 sollte nicht leer sein", 0, file1.getSize());
    }
}