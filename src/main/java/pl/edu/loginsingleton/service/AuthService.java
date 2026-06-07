package pl.edu.loginsingleton.service;

import pl.edu.loginsingleton.dao.UserDao;
import pl.edu.loginsingleton.model.User;
import pl.edu.loginsingleton.session.SessionManager;
import pl.edu.loginsingleton.util.InputValidator;

import java.util.Optional;

public class AuthService {

    private final UserDao userDao;
    private final SessionManager session;

    public AuthService() {
        this.userDao = new UserDao();
        this.session = SessionManager.getInstance();
    }

    public User register(String username, String password) {
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);

        String login = username.trim();
        if (userDao.existsByUsername(login)) {
            throw new AuthException("Uzytkownik o loginie '" + login + "' juz istnieje.");
        }
        return userDao.insert(login, password);
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new AuthException("Login i haslo nie moga byc puste.");
        }

        String login = username.trim();
        Optional<User> found = userDao.findByUsername(login);
        if (found.isEmpty() || !found.get().getPassword().equals(password)) {
            throw new AuthException("Niepoprawny login lub haslo.");
        }

        User user = found.get();
        session.login(user);
        return user;
    }

    public void logout() {
        session.logout();
    }
}
