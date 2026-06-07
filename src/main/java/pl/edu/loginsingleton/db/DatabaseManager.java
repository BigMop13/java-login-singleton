package pl.edu.loginsingleton.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_DIRECTORY = "data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIRECTORY + "/login.db";

    private static volatile DatabaseManager instance;

    private final Connection connection;

    private DatabaseManager() {
        try {
            File dir = new File(DB_DIRECTORY);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            this.connection = DriverManager.getConnection(DB_URL);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Nie udalo sie polaczyc z baza danych: " + e.getMessage(), e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void initSchema() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    username   TEXT NOT NULL UNIQUE,
                    password   TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas zamykania polaczenia: " + e.getMessage());
        }
    }
}
