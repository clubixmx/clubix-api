package com.clubix.api.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Seeds the local H2 database with a named scenario on startup.
 *
 * <p>Only active for the {@code local} profile. The scenario is selected
 * via the {@code app.scenario} property. Each scenario is a SQL file
 * located at {@code classpath:db/scenarios/<name>.sql}.
 *
 * <p>To disable seeding, set {@code app.scenario=} (empty).
 * To switch scenarios in IntelliJ: Run → Edit Configurations → Environment variables
 *   APP_SCENARIO=high-balance
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class ScenarioSeeder implements ApplicationRunner {

    private final DatabaseClient db;

    @Value("${app.scenario:}")
    private String scenario;

    @Override
    public void run(ApplicationArguments args) {
        if (scenario == null || scenario.isBlank()) {
            log.info("[Scenario] No scenario configured (app.scenario). Skipping seed.");
            return;
        }

        String path = "db/scenarios/" + scenario + ".sql";
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            throw new IllegalStateException(
                    "[Scenario] File not found: " + path + ". " +
                    "Create src/main/resources/" + path);
        }

        log.info("[Scenario] Loading scenario '{}' from {}", scenario, path);

        try {
            String sql = resource.getContentAsString(StandardCharsets.UTF_8);

            Flux.fromArray(sql.split(";"))
                    .map(String::trim)
                    .filter(stmt -> !stmt.isBlank())
                    .concatMap(stmt -> db.sql(stmt).fetch().rowsUpdated())
                    .then()
                    .doOnSuccess(v -> log.info("[Scenario] '{}' loaded successfully.", scenario))
                    .block();

        } catch (IOException e) {
            throw new IllegalStateException("[Scenario] Failed to read " + path, e);
        }
    }
}

