package pl.edu.loginsingleton.service;

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
