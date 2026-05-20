package com.bedsheet.orders.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * EnvironmentPostProcessor that detects Heroku-style database URLs (DATABASE_URL, CLEARDB_DATABASE_URL,
 * JAWSDB_URL or JDBC_DATABASE_URL) and populates Spring datasource properties so the application can
 * connect without requiring DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD env vars to be set separately.
 */
public class HerokuDatasourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String[] CANDIDATE_ENV = new String[] {
            "JDBC_DATABASE_URL",
            "CLEARDB_DATABASE_URL",
            "JAWSDB_URL",
            "DATABASE_URL"
    };

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, org.springframework.boot.SpringApplication application) {
        for (String envName : CANDIDATE_ENV) {
            String raw = System.getenv(envName);
            if (raw == null || raw.isBlank()) {
                continue;
            }

            // If already a JDBC URL (starts with jdbc:), prefer it directly
            if (raw.startsWith("jdbc:")) {
                applyProps(environment, raw, null, null);
                return;
            }

            // Try to parse URIs like: mysql://user:pass@host:port/dbname?params
            try {
                URI uri = new URI(raw);
                String userInfo = uri.getUserInfo();
                String username = null;
                String password = null;
                if (userInfo != null) {
                    String[] up = userInfo.split(":", 2);
                    username = up[0];
                    if (up.length > 1) password = up[1];
                }

                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath(); // /dbname
                String db = (path != null && path.startsWith("/")) ? path.substring(1) : path;
                String query = uri.getQuery();

                StringBuilder jdbc = new StringBuilder();
                jdbc.append("jdbc:mysql://");
                jdbc.append(host == null ? "" : host);
                if (port != -1) {
                    jdbc.append(":").append(port);
                }
                jdbc.append("/");
                if (db != null) jdbc.append(db);
                if (query != null && !query.isBlank()) {
                    jdbc.append("?").append(query);
                }
                // ensure some sane defaults for timezone & public key retrieval which are commonly needed on MySQL 8+
                String jdbcUrl = jdbc.toString();
                if (!jdbcUrl.contains("serverTimezone")) {
                    jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "serverTimezone=UTC";
                }
                if (!jdbcUrl.contains("allowPublicKeyRetrieval")) {
                    jdbcUrl += "&allowPublicKeyRetrieval=true";
                }

                applyProps(environment, jdbcUrl, username, password);
                System.out.println("HerokuDatasourceEnvironmentPostProcessor: applied JDBC URL from env " + envName + ": " + jdbcUrl);
                return;
            } catch (Exception ex) {
                // ignore and try next candidate
            }
        }

        // No Heroku-style DB URL found — apply an in-memory H2 fallback so the application can start
        try {
            String h2Url = "jdbc:h2:mem:bedsheet_orders;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
            System.out.println("HerokuDatasourceEnvironmentPostProcessor: no external DB found, applying H2 fallback: " + h2Url);
            applyProps(environment, h2Url, "sa", "");
        } catch (Exception ex) {
            // shouldn't happen, but don't prevent app startup
            System.out.println("HerokuDatasourceEnvironmentPostProcessor: failed to apply H2 fallback: " + ex.getMessage());
        }
    }

    private void applyProps(ConfigurableEnvironment env, String jdbcUrl, String username, String password) {
        Map<String, Object> m = new HashMap<>();
        // Only set properties if not already defined in environment
        if (env.getProperty("spring.datasource.url") == null) {
            m.put("spring.datasource.url", jdbcUrl);
        }
        // set a sensible driver class if absent
        if (env.getProperty("spring.datasource.driver-class-name") == null) {
            if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2")) {
                m.put("spring.datasource.driver-class-name", "org.h2.Driver");
            }
        }
        if (username != null && env.getProperty("spring.datasource.username") == null) {
            m.put("spring.datasource.username", username);
        }
        if (password != null && env.getProperty("spring.datasource.password") == null) {
            m.put("spring.datasource.password", password);
        }
        // If using H2, force JPA to use the H2 dialect and enable schema auto-update by default.
        // We intentionally override existing JPA dialect/ddl settings when we fallback to H2 so
        // Hibernate generates H2-compatible DDL instead of MySQL-specific SQL which H2 can't parse.
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2")) {
            m.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
            m.put("spring.jpa.hibernate.ddl-auto", "update");
        }

        if (!m.isEmpty()) {
            System.out.println("HerokuDatasourceEnvironmentPostProcessor: injecting properties: " + m.keySet());
            env.getPropertySources().addFirst(new MapPropertySource("heroku-database", m));
        }
    }
}

