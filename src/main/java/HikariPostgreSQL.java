import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HikariPostgreSQL {

    private static HikariDataSource ds;

    public HikariPostgreSQL() {
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null) {
            // System.out.println("Creating Hikari Pool");
            ds = getDataSource(getPostgresCredentials());
        }
        // System.out.println("Returning SQL/Hikari Connection");
        return ds.getConnection();
    }

    private static Map<String, Object> getPostgresCredentials() {

        Map<String, Object> map = new HashMap<>();

        Path credentialsPath = Paths.get("credentials.json").toAbsolutePath();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(Files.newInputStream(credentialsPath, StandardOpenOption.READ));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = node.get("postgresqlhome").get("host").asText();
        int port = node.get("postgresqlhome").get("port").asInt();
        String database = node.get("postgresqlhome").get("database").asText();
        String username = node.get("postgresqlhome").get("username").asText();
        String password = node.get("postgresqlhome").get("password").asText();

        map.put("host", host);
        map.put("port", port);
        map.put("database", database);
        map.put("username", username);
        map.put("password", password);

        return map;
    }


    private static HikariDataSource getDataSource(Map<String, Object> credentials) {


        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://" + credentials.get("host") + ":" + credentials.get("port") + "/" + credentials.get("database"));
        config.setUsername(credentials.get("username").toString());
        config.setPassword(credentials.get("password").toString());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setLeakDetectionThreshold(0);
        config.setConnectionInitSql("SELECT 1");
        config.setReadOnly(false);
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(3);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        HikariDataSource ds = new HikariDataSource(config);

        return ds;
    }


}
