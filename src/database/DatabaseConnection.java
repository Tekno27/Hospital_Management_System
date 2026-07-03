package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private static final String DB_FILE_NAME = "hospital.db";
    private static volatile boolean schemaEnsured;

    private DatabaseConnection() {
    }

    /** Call once at application startup before any screen needs the database. */
    public static void initialize() {
        getConnection();
    }

    public static Path getDatabasePath() {
        return Path.of(System.getProperty("user.dir")).resolve(DB_FILE_NAME).toAbsolutePath();
    }

    public static Connection getConnection() {
        try {
            Path dbPath = getDatabasePath();
            boolean isNewDb = !Files.exists(dbPath);
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            ensureSchema(connection, isNewDb);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database at " + getDatabasePath() + ": " + e.getMessage(), e);
        }
    }

    private static synchronized void ensureSchema(Connection connection, boolean isNewDb) {
        if (schemaEnsured) {
            return;
        }
        try {
            executeSqlScript(connection, readSchemaFile());
            schemaEnsured = true;
            if (isNewDb) {
                System.out.println("Database initialized: " + getDatabasePath());
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Database setup failed: " + e.getMessage(), e);
        }
    }

    private static void executeSqlScript(Connection connection, String sql) {
        List<String> statements = splitSqlStatements(sql);
        try (Statement statement = connection.createStatement()) {
            for (String singleStatement : statements) {
                statement.execute(singleStatement);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error: " + e.getMessage(), e);
        }
    }

    /**
     * Splits SQL on semicolons that are not inside single-quoted strings.
     */
    static List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;

        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'') {
                inSingleQuote = !inSingleQuote;
                current.append(ch);
                continue;
            }
            if (ch == ';' && !inSingleQuote) {
                String trimmed = current.toString().trim();
                if (!trimmed.isEmpty()) {
                    statements.add(trimmed);
                }
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }

        String trimmed = current.toString().trim();
        if (!trimmed.isEmpty()) {
            statements.add(trimmed);
        }
        return statements;
    }

    private static String readSchemaFile() {
        try (InputStream inputStream = DatabaseConnection.class.getResourceAsStream("/database/schema.sql")) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes());
            }
        } catch (IOException ignored) {
        }

        Path[] candidates = {
                Path.of("src", "database", "schema.sql"),
                Path.of("database", "schema.sql")
        };

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                try {
                    return Files.readString(candidate);
                } catch (IOException ignored) {
                }
            }
        }

        throw new RuntimeException(
                "Could not find schema.sql. Rebuild the project so src/database/schema.sql is on the classpath.");
    }

    public static void closeConnection() {
        // connections are short-lived per DAO call
    }
}
