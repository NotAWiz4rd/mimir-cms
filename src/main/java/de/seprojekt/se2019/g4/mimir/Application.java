package de.seprojekt.se2019.g4.mimir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Spring will scan for 'controllers', 'repositories', 'services' etc. in the package (and its subpackages)
 * of this class. These classes will be initiated once (like a singleton) and will be autowired to the
 * constructors of requesting classes (dependency injection).
 * https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring
 * https://www.baeldung.com/constructor-injection-in-spring
 */
@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Application {

    /**
     * Start Spring Framework with our application.
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}