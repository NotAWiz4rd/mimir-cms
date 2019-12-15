package de.seprojekt.se2019.g4.mimir.content.comment;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.seprojekt.se2019.g4.mimir.content.artifact.ArtifactRepository;
import de.seprojekt.se2019.g4.mimir.security.user.UserService;

@RestController
public class CommentController {
    @Autowired
    private ArtifactRepository artifactRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserService userService;

    @PostMapping
    public Comment create(Principal principal, @Valid @RequestBody CreateCommentDto comment) throws IOException {
        return artifactRepository.findById(comment.getArtifactId())
            .map(artifact -> {
                if (!userService.isAuthorizedForSpace(artifact.getSpace(), principal)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to space");
                }
                var user = userService.findByName(principal.getName()).get();
                return commentRepository.save(new Comment(artifact, comment.getText(), user, Instant.now()));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Artifact does not exist"));
    }

    @GetMapping
    public Collection<Comment> list(Principal principal, @RequestParam("artifactId") Long artifactId) throws IOException {
        return artifactRepository.findById(artifactId)
            .map(artifact -> {
                if (!userService.isAuthorizedForSpace(artifact.getSpace(), principal)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to space");
                }
                return commentRepository.findByArtifactId(artifactId);
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Artifact does not exist"));
    }

}
