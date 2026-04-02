package com.clubix.api.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

/**
 * Starts H2's built-in Web console for the local profile.
 *
 * <p>Since this app uses Spring WebFlux (reactive), the standard
 * spring.h2.console servlet-based console is not available.
 * Instead, H2's own built-in web server is launched on port 8082.
 *
 * <p>Access the console at: http://localhost:8082
 * Connect with:
 *   JDBC URL : jdbc:h2:mem:clubixdb
 *   User Name: sa
 *   Password : (empty)
 *
 * <p>The web server runs embedded in the same JVM, so it can reach
 * the in-memory database directly — no TCP server required.
 */
@Configuration
@Profile("local")
public class H2ServerConfig {

    /**
     * H2 Web console — accessible at http://localhost:8082.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws SQLException {
        return Server.createWebServer(
                "-web",
                "-webAllowOthers",
                "-webPort", "8082"
        );
    }
}
