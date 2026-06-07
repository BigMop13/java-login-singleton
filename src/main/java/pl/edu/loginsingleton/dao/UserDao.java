package pl.edu.loginsingleton.dao;

import pl.edu.loginsingleton.db.DatabaseManager;
import pl.edu.loginsingleton.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDao {

    private final Connection connection;

    public UserDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, created_at FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Blad odczytu uzytkownika z bazy: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public User insert(String username, String password) {
        String createdAt = LocalDateTime.now().toString();
        String sql = "INSERT INTO users (username, password, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, createdAt);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                return new User(id, username, password, createdAt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Blad zapisu uzytkownika do bazy: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("created_at"));
    }
}
