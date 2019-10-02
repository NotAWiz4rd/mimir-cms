package de.seprojekt.se2019.g4.mimir.content.artifact;

import de.seprojekt.se2019.g4.mimir.content.ContentHelper;
import de.seprojekt.se2019.g4.mimir.content.ContentService;
import de.seprojekt.se2019.g4.mimir.content.folder.Folder;
import de.seprojekt.se2019.g4.mimir.content.folder.FolderRepository;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailContentStore;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest()
@Transactional
@ActiveProfiles("test")
public class ArtifactServiceIT {

    @Autowired
    ArtifactRepository artifactRepository;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    ArtifactContentStore artifactContentStore;

    @Autowired
    ThumbnailContentStore thumbnailContentStore;

    @Autowired
    ThumbnailGenerator thumbnailGenerator;

    @Autowired
    ContentService contentService;

    ArtifactService artifactService;

    Artifact artifact1;
    Artifact artifact2;
    Folder folder3;
    Artifact artifact4;
    Folder folder5;
    Artifact artifact6;

    @Before
    public void setUp() {
        artifactService = new ArtifactService(artifactRepository, artifactContentStore, thumbnailContentStore, thumbnailGenerator, contentService);

        /* Wir haben folgende Dateien:
         *  /artifact1
         *  /artifact2
         *  /folder3/artifact4
         *  /folder3/folder5/artifact6
         */
        artifact1 = artifactRepository.save(ContentHelper.createArtifact("/", "artifact1"));
        artifact2 = artifactRepository.save(ContentHelper.createArtifact("/", "artifact2"));
        folder3 = folderRepository.save(ContentHelper.createFolder("/", "folder3"));

        artifact4 = artifactRepository.save(ContentHelper.createArtifact("/folder3/", "artifact4"));

        folder5 = folderRepository.save(ContentHelper.createFolder("/folder3/", "folder5"));
        artifact6 = artifactRepository.save(ContentHelper.createArtifact("/folder3/folder5/", "artifact6"));
    }

    @Test
    public void findArtifact_forArtifact1_returnArtifact1() {
        Optional<Artifact> artifact = artifactService.findByTotalUrl("/artifact1");
        assertTrue(artifact.isPresent());
        assertEquals(artifact1, artifact.get());
    }

    @Test
    public void findArtifact_forArtifact2_returnArtifact2() {
        Optional<Artifact> artifact = artifactService.findByTotalUrl("/artifact2");
        assertTrue(artifact.isPresent());
        assertEquals(artifact2, artifact.get());
    }

    @Test
    public void findArtifact_forArtifact4_returnArtifact4() {
        Optional<Artifact> artifact = artifactService.findByTotalUrl("/folder3/artifact4");
        assertTrue(artifact.isPresent());
        assertEquals(artifact4, artifact.get());
    }

    @Test
    public void findArtifact_forArtifact6_returnArtifact6() {
        Optional<Artifact> artifact = artifactService.findByTotalUrl("/folder3/folder5/artifact6");
        assertTrue(artifact.isPresent());
        assertEquals(artifact6, artifact.get());
    }

    @Test
    public void findArtifact_withInvalidArtifactUrl_returnEmptyOptional() {
        Optional<Artifact> artifact = artifactService.findByTotalUrl("/geheimes_bild");
        assertFalse(artifact.isPresent());
    }

    @Test
    public void initialCheckin_addTextFile_tryToAccessFile() throws IOException {
        String username = "thellmann";
        Principal mockedPrincipal = mock(Principal.class);
        when(mockedPrincipal.getName()).thenReturn(username);

        String text = "Laborum odio tempore consequatur eos.";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        MultipartFile mockedFile = new MockMultipartFile("test.txt", "test.txt", MediaType.TEXT_PLAIN_VALUE, inputStream);

        Artifact artifact = artifactService.initialCheckin("my_text_file", mockedFile, "/", mockedPrincipal);

        InputStream fileOfArtifact = artifactService.findArtifact(artifact);
        String actual = IOUtils.toString(fileOfArtifact, StandardCharsets.UTF_8);

        assertEquals(text, actual);
    }
}