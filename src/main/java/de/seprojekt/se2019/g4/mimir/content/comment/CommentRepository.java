package de.seprojekt.se2019.g4.mimir.content.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArtifactId(Long artifactId);

}
