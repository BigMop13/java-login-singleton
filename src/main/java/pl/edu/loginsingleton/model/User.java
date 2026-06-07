package pl.edu.loginsingleton.model;

public class User {

    private final int id;
    private final String username;
    private final String password;
    private final String createdAt;

    public User(int id, String username, String password, String createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', createdAt='" + createdAt + "'}";
    }
}
