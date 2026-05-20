package com.bedsheet.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BedsheetOrderApplication {

    public static void main(String[] args) {
        // Configure datasource properties from common Heroku env vars if present
        configureDatasourceFromHerokuEnv();
        SpringApplication.run(BedsheetOrderApplication.class, args);
    }

    private static void configureDatasourceFromHerokuEnv() {
        // If explicit Spring datasource env vars are present, prefer them and skip legacy URL parsing.
        if (hasText(System.getenv("SPRING_DATASOURCE_URL"))
                || hasText(System.getenv("SPRING_DATASOURCE_USERNAME"))
                || hasText(System.getenv("SPRING_DATASOURCE_PASSWORD"))) {
            return;
        }

        String[] candidates = new String[]{"JDBC_DATABASE_URL", "CLEARDB_DATABASE_URL", "JAWSDB_URL", "DATABASE_URL"};
        for (String name : candidates) {
            String raw = System.getenv(name);
            if (raw == null || raw.isBlank()) continue;
            // If JDBC style, set directly
            if (raw.startsWith("jdbc:")) {
                System.setProperty("spring.datasource.url", raw);
                return;
            }
            try {
                java.net.URI uri = new java.net.URI(raw);
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
                String path = uri.getPath();
                String db = (path != null && path.startsWith("/")) ? path.substring(1) : path;
                String query = uri.getQuery();
                StringBuilder jdbc = new StringBuilder();
                jdbc.append("jdbc:mysql://");
                jdbc.append(host == null ? "" : host);
                if (port != -1) jdbc.append(":").append(port);
                jdbc.append("/");
                if (db != null) jdbc.append(db);
                if (query != null && !query.isBlank()) jdbc.append("?").append(query);
                String jdbcUrl = jdbc.toString();
                if (!jdbcUrl.contains("serverTimezone")) {
                    jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "serverTimezone=UTC";
                }
                if (!jdbcUrl.contains("allowPublicKeyRetrieval")) {
                    jdbcUrl += "&allowPublicKeyRetrieval=true";
                }
                // Only set properties if not already set
                if (System.getProperty("spring.datasource.url") == null) {
                    System.setProperty("spring.datasource.url", jdbcUrl);
                }
                if (username != null && System.getProperty("spring.datasource.username") == null) {
                    System.setProperty("spring.datasource.username", username);
                }
                if (password != null && System.getProperty("spring.datasource.password") == null) {
                    System.setProperty("spring.datasource.password", password);
                }
                return;
            } catch (Exception ex) {
                // ignore and try next
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
