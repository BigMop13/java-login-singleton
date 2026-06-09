## 1. Opis projektu

Konsolowa aplikacja w języku **Java**, która realizuje prosty **system logowania**:
rejestracja użytkowników, logowanie, podgląd aktualnej sesji oraz wylogowanie. Dane
użytkowników przechowywane są w bazie **SQLite** (przez JDBC), dzięki czemu konta są
trwałe między kolejnymi uruchomieniami programu.

Celem dydaktycznym projektu jest praktyczna demonstracja wzorca projektowego
**Singleton** w realnym kontekście aplikacji.

## 2. Wzorzec projektowy Singleton

**Singleton** to kreacyjny wzorzec projektowy, który gwarantuje, że dana klasa ma
**dokładnie jedną instancję** w całej aplikacji oraz zapewnia globalny punkt dostępu
do tej instancji.

## 3. Architektura i struktura projektu

Projekt podzielono na warstwy (model → dostęp do danych → logika → interfejs):

```
java-login-singleton/
├── pom.xml                          # konfiguracja Maven
├── README.md                        # ten plik
├── data/
│   └── login.db                     # baza SQLite (tworzona automatycznie)
└── src/main/java/pl/edu/loginsingleton/
    ├── Main.java                    # punkt wejścia
    ├── model/
    │   └── User.java                # model danych użytkownika (POJO)
    ├── db/
    │   └── DatabaseManager.java     # ★ SINGLETON – połączenie z SQLite
    ├── dao/
    │   └── UserDao.java             # operacje CRUD (PreparedStatement)
    ├── service/
    │   ├── AuthService.java         # logika rejestracji/logowania
    │   └── AuthException.java       # wyjątek domenowy
    ├── session/
    │   └── SessionManager.java      # ★ SINGLETON – sesja użytkownika
    ├── util/
    │   └── InputValidator.java      # walidacja loginu i hasła
    └── ui/
        └── ConsoleMenu.java         # konsolowe menu (pętla programu)
```

### Diagram klas (UML)

```
              ┌─────────────────┐
              │      Main       │
              └────────┬────────┘
                       │ uruchamia
                       ▼
              ┌─────────────────┐        korzysta z      ┌──────────────────────┐
              │   ConsoleMenu   │───────────────────────►│   SessionManager     │
              └────────┬────────┘                        │   «Singleton»        │
                       │ używa                           │ - currentUser: User  │
                       ▼                                 │ + getInstance()      │
              ┌─────────────────┐      używa             └──────────────────────┘
              │   AuthService   │───────────┐                        ▲
              └────────┬────────┘           │                        │ login()/logout()
                       │ używa              ▼                        │
                       ▼            ┌────────────────┐                │
              ┌─────────────────┐   │ InputValidator │                │
              │     UserDao     │   │  (walidacja)   │                │
              └────────┬────────┘   └────────────────┘                │
                       │ pobiera połączenie                           │
                       ▼                                              │
              ┌──────────────────────┐                               │
              │   DatabaseManager    │           AuthService ─────────┘
              │   «Singleton»        │
              │ - connection: Conn   │           ┌──────────┐
              │ + getInstance()      │  zwraca   │   User   │
              │ + getConnection()    │◄──────────│  (model) │
              └──────────────────────┘           └──────────┘
```

## 4. Wymagania

- **Java 21** (lub nowsza)
- **Apache Maven 3.9+**
- Sterownik **sqlite-jdbc 3.49.1.0** (pobierany automatycznie przez Maven)

## 5. Uruchomienie

```bash
mvn compile
mvn exec:java
```
