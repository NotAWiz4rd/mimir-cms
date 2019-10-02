package de.seprojekt.se2019.g4.mimir.content.artifact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * This interface will be implemented by Spring Data JPA.
 * The implementation will help us to store {@link Artifact} in the database and execute CRUD (create, read, update,
 * delete) operation on the database.
 * The name of the methods defines how Spring Data JPA will implement them - e.g. findByParentUrl will search for
 * all entries from the artifact table where parentUrl matches with the parameter parentUrl.
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.core-concepts.
 */
public interface ArtifactRepository extends JpaRepository<Artifact, Long> {

    List<Artifact> findByParentUrl(String parentUrl);

    List<Artifact> findByParentUrlStartingWith(String parentUrl);

    Optional<Artifact> findByTotalUrl(String totalUrl);

    boolean existsByTotalUrl(String totalUrl);

    boolean existsByParentUrlStartingWith(String parentUrl);

}
