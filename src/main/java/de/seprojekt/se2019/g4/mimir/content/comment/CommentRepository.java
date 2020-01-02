package de.seprojekt.se2019.g4.mimir.content.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByArtifactId(Long artifactId);
}
