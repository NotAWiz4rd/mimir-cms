package de.seprojekt.se2019.g4.mimir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.seprojekt.se2019.g4.mimir.content.artifact.Artifact;
import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactService;
import de.seprojekt.se2019.g4.mimir.content.comment.Comment;
import de.seprojekt.se2019.g4.mimir.content.comment.CommentController;
import de.seprojekt.se2019.g4.mimir.content.comment.CreateCommentDto;
import de.seprojekt.se2019.g4.mimir.content.space.Space;
import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import de.seprojekt.se2019.g4.mimir.security.user.User;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.io.IOException;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CommentControllerTest {

  @Autowired
  CommentController commentController;

  @Autowired
  ArtifactService artifactService;

  @Autowired
  UserService userService;

  private User user;
  private Space space;

  @Before
  public void init() {
    user = userService.findByName("thellmann").get();
    space = user.getSpaces().get(0);
  }

  @Test
  public void shouldCreateListDeleteComment() throws IOException {
    String name = "file1.txt";
    Artifact artifact = artifactService.create(
        name,
        new MockMultipartFile(name, name, "text/plain", "foobar".getBytes()),
        space.getRootFolder()
    );

    String text = "Basic comment text.";
    CreateCommentDto commentDto = new CreateCommentDto();
    commentDto.setArtifactId(artifact.getId());
    commentDto.setText(text);

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        new JwtPrincipal("thellmann"), "");
    Comment comment = commentController.create(auth, commentDto);

    assertNotNull("should exist", comment);
    assertEquals("should have right id", artifact.getId(), comment.getArtifact().getId());
    assertEquals("should have right author", user, comment.getAuthor());
    assertEquals("should have right author", text, comment.getText());

    assertTrue("should list comment",
        commentController.list(auth, artifact.getId()).contains(comment));

    commentController.delete(auth, comment.getId());
    assertTrue("should delete comment", commentController.list(auth, artifact.getId()).isEmpty());
  }
}