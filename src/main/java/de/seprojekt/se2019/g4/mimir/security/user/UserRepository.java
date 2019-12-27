package de.seprojekt.se2019.g4.mimir.security.user;

import de.seprojekt.se2019.g4.mimir.content.space.Space;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByName(String name);

  Optional<User> findByMail(String mail);

  Integer countBySpacesContains(Space space);

  List<User> findAllBySpacesContains(Space space);
}
