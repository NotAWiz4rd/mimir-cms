package de.seprojekt.se2019.g4.mimir.content.comment;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactRepository;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;
import java.security.Principal;
import java.time.Instant;
import java.util.Collection;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * This controller offers an HTTP interface for manipulating comments (e.g. deleting, creating
 * etc.)
 */
@RestController
@RequestMapping("comments")
public class CommentController {

  private final static Logger LOGGER = LoggerFactory.getLogger(CommentController.class);
  private final ArtifactRepository artifactRepository;
  private final CommentRepository commentRepository;
  private final UserService userService;

  /**
   * The parameters will be autowired by Spring.
   */
  public CommentController(ArtifactRepository artifactRepository,
      CommentRepository commentRepository, UserService userService) {
    this.artifactRepository = artifactRepository;
    this.commentRepository = commentRepository;
    this.userService = userService;
  }

  /**
   * The user can create a new comment for an existing artifact by calling this interface.
   */
  @PostMapping
  public Comment create(Principal principal, @Valid @RequestBody CreateCommentDto comment) {
    return artifactRepository.findById(comment.getArtifactId())
        .map(artifact -> {
          if (!userService.isAuthorizedForArtifact(artifact, principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
          }

          var user = userService.findByName(principal.getName());
          if(user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
          }

          LOGGER.info("Saving new comment for '{}'", artifact.getName());

          return commentRepository
              .save(new Comment(artifact, comment.getText(), user.get(), Instant.now()));
        })
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
  }

  /**
   * The user can get the list of comments for an existing artifact by calling this interface.
   */
  @GetMapping
  public Collection<Comment> list(Principal principal,
      @RequestParam("artifactId") Long artifactId) {
    return artifactRepository.findById(artifactId)
        .map(artifact -> {
          if (!userService.isAuthorizedForArtifact(artifact, principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
          }
          return commentRepository.findByArtifactId(artifactId);
        })
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
  }

  /**
   * The user can delete a comment for an existing artifact if he is the author of the comment.
   */
  @DeleteMapping("{id}")
  public void delete(Principal principal, @PathVariable Long id) {
    var comment = commentRepository.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!comment.getAuthor().getName().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not author of comment");
    }
    LOGGER.info("Deleting comment for '{}'", comment.getArtifact().getName());
    commentRepository.delete(comment);
  }
}
