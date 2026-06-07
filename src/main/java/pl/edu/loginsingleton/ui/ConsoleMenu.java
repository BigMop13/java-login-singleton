package pl.edu.loginsingleton.ui;

import pl.edu.loginsingleton.db.DatabaseManager;
import pl.edu.loginsingleton.model.User;
import pl.edu.loginsingleton.service.AuthException;
import pl.edu.loginsingleton.service.AuthService;
import pl.edu.loginsingleton.session.SessionManager;

import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = new AuthService();
    private final SessionManager session = SessionManager.getInstance();

    public void start() {
        System.out.println("=========================================");
        System.out.println("  System logowania (wzorzec Singleton)");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleRegister();
                case "2" -> handleLogin();
                case "3" -> handleShowCurrent();
                case "4" -> handleLogout();
                case "0" -> running = false;
                default -> System.out.println("[!] Nieznana opcja. Sprobuj ponownie.");
            }
        }

        shutdown();
    }

    private void printMenu() {
        String status = session.isLoggedIn()
                ? "zalogowany jako: " + session.getCurrentUser().getUsername()
                : "niezalogowany";
        System.out.println();
        System.out.println("----- MENU (" + status + ") -----");
        System.out.println("  1. Rejestracja");
        System.out.println("  2. Logowanie");
        System.out.println("  3. Pokaz zalogowanego uzytkownika");
        System.out.println("  4. Wylogowanie");
        System.out.println("  0. Wyjscie");
        System.out.print("Wybierz opcje: ");
    }

    private void handleRegister() {
        System.out.print("Podaj login: ");
        String username = scanner.nextLine();
        System.out.print("Podaj haslo: ");
        String password = scanner.nextLine();
        try {
            User user = authService.register(username, password);
            System.out.println("[OK] Zarejestrowano uzytkownika: " + user.getUsername());
        } catch (AuthException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private void handleLogin() {
        if (session.isLoggedIn()) {
            System.out.println("[!] Jestes juz zalogowany jako "
                    + session.getCurrentUser().getUsername() + ". Najpierw sie wyloguj.");
            return;
        }
        System.out.print("Podaj login: ");
        String username = scanner.nextLine();
        System.out.print("Podaj haslo: ");
        String password = scanner.nextLine();
        try {
            User user = authService.login(username, password);
            System.out.println("[OK] Zalogowano. Witaj, " + user.getUsername() + "!");
        } catch (AuthException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private void handleShowCurrent() {
        if (session.isLoggedIn()) {
            System.out.println("[i] Aktualna sesja: " + session.getCurrentUser());
        } else {
            System.out.println("[i] Brak zalogowanego uzytkownika.");
        }
    }

    private void handleLogout() {
        if (session.isLoggedIn()) {
            String name = session.getCurrentUser().getUsername();
            authService.logout();
            System.out.println("[OK] Wylogowano uzytkownika: " + name);
        } else {
            System.out.println("[!] Nikt nie jest zalogowany.");
        }
    }

    private void shutdown() {
        DatabaseManager.getInstance().close();
        scanner.close();
        System.out.println("Do zobaczenia!");
    }
}
