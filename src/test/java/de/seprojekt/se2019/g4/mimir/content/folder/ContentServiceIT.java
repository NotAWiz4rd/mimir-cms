package de.seprojekt.se2019.g4.mimir.content.folder;

import de.seprojekt.se2019.g4.mimir.content.Content;
import de.seprojekt.se2019.g4.mimir.content.ContentHelper;
import de.seprojekt.se2019.g4.mimir.content.ContentService;
import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactContentStore;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactRepository;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailContentStore;
import de.seprojekt.se2019.g4.mimir.content.thumbnail.ThumbnailGenerator;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest()
@Transactional
@ActiveProfiles("test")
public class ContentServiceIT {

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

    ContentService contentService;

    Artifact artifact1;
    Artifact artifact2;
    Folder folder3;
    Artifact artifact4;
    Folder folder5;
    Artifact artifact6;

    @Before
    public void setUp() {
        contentService = new ContentService(folderRepository, artifactRepository);

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
    public void findContentForFolder_inRoot_returnRootView() {
        Optional<List<Content>> contentForFolder = contentService.findContentForFolder("/");
        assertTrue(contentForFolder.isPresent());

        List<Content> contentList = contentForFolder.get();
        assertThat(contentList, Matchers.containsInAnyOrder(artifact1, artifact2, folder3));
    }

    @Test
    public void findContentForFolder_inFolder3_returnFolder3View() {
        Optional<List<Content>> contentForFolder = contentService.findContentForFolder("/folder3/");
        assertTrue(contentForFolder.isPresent());

        List<Content> contentList = contentForFolder.get();
        assertThat(contentList, Matchers.containsInAnyOrder(artifact4, folder5));
    }

    @Test
    public void findContentForFolder_inFolder5_returnFolder5View() {
        Optional<List<Content>> contentForFolder = contentService.findContentForFolder("/folder3/folder5/");
        assertTrue(contentForFolder.isPresent());

        List<Content> contentList = contentForFolder.get();
        assertThat(contentList, Matchers.containsInAnyOrder(artifact6));
    }

    @Test
    public void findContentForFolder_withInvalidFolderUrl_returnEmptyOptional() {
        Optional<List<Content>> contentForFolder = contentService.findContentForFolder("/test123/");
        assertFalse(contentForFolder.isPresent());
    }
}