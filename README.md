# System logowania z użyciem wzorca Singleton

**Przedmiot:** Języki obiektowe II (Java)
**Prowadzący:** mgr inż. Michał Niemczyk
**Rok akademicki:** 2025/2026 (semestr letni)
**Temat projektu:** System logowania z użyciem wzorca projektowego **Singleton**

---

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

**Cechy implementacji (w tym projekcie):**
- **prywatny konstruktor** – uniemożliwia tworzenie obiektów z zewnątrz (`new`),
- **prywatne, statyczne pole** `instance` przechowujące jedyny obiekt,
- **publiczna, statyczna metoda** `getInstance()` zwracająca zawsze ten sam obiekt.

Zastosowano wariant **thread-safe „double-checked locking"** z polem `volatile`:

```java
private static volatile DatabaseManager instance;

public static DatabaseManager getInstance() {
    if (instance == null) {                       // 1. szybkie sprawdzenie bez blokady
        synchronized (DatabaseManager.class) {    // 2. blokada tylko przy pierwszym tworzeniu
            if (instance == null) {               // 3. ponowne sprawdzenie wewnątrz blokady
                instance = new DatabaseManager();
            }
        }
    }
    return instance;
}
```

Słowo kluczowe `volatile` gwarantuje, że zapis nowej instancji jest natychmiast
widoczny dla wszystkich wątków, co czyni ten wariant bezpiecznym w środowisku
wielowątkowym.

**Inne sposoby implementacji Singletona** (warte wspomnienia):
- *eager initialization* – `private static final Instance INSTANCE = new Instance();`,
- *initialization-on-demand holder idiom* – leniwa inicjalizacja przez klasę wewnętrzną,
- *enum singleton* – najbezpieczniejszy wariant wg Joshuy Blocha („Effective Java").

### Gdzie Singleton występuje w projekcie?

| Klasa | Rola | Dlaczego Singleton? |
|-------|------|---------------------|
| `DatabaseManager` | Zarządza **jednym** połączeniem z bazą SQLite oraz schematem | W całej aplikacji potrzebne jest jedno, współdzielone połączenie do bazy |
| `SessionManager` | Przechowuje **stan bieżącej sesji** (zalogowany użytkownik) | W aplikacji istnieje tylko jedna, globalna sesja użytkownika |

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

### Wariant A – przez Maven (zalecany do uruchamiania w trakcie pracy)

```bash
mvn compile
mvn exec:java
```

### Wariant B – jako samodzielny plik JAR (do oddania / dystrybucji)

```bash
mvn package
java -jar target/login-singleton-1.0.0-shaded.jar
```

> Plik `*-shaded.jar` to tzw. *fat jar* – zawiera w sobie sterownik SQLite,
> więc działa bez dodatkowej konfiguracji ścieżki klas.

Przy pierwszym uruchomieniu w katalogu `data/` automatycznie tworzony jest plik
bazy `login.db`.

## 6. Obsługa programu

Po uruchomieniu pojawia się menu:

```
----- MENU (niezalogowany) -----
  1. Rejestracja
  2. Logowanie
  3. Pokaz zalogowanego uzytkownika
  4. Wylogowanie
  0. Wyjscie
```

### Zasady walidacji danych

| Pole  | Reguły |
|-------|--------|
| Login | 3–20 znaków, dozwolone tylko litery, cyfry oraz `_`; login musi być unikalny |
| Hasło | minimum 6 znaków |

## 7. Przykładowa sesja działania

```
----- MENU (niezalogowany) -----
Wybierz opcje: 1
Podaj login: ab
Podaj haslo: haslo123
[!] Login musi miec od 3 do 20 znakow.

Wybierz opcje: 1
Podaj login: jan_kowalski
Podaj haslo: haslo123
[OK] Zarejestrowano uzytkownika: jan_kowalski

Wybierz opcje: 1
Podaj login: jan_kowalski
Podaj haslo: inne123
[!] Uzytkownik o loginie 'jan_kowalski' juz istnieje.

Wybierz opcje: 2
Podaj login: jan_kowalski
Podaj haslo: zlehaslo
[!] Niepoprawny login lub haslo.

Wybierz opcje: 2
Podaj login: jan_kowalski
Podaj haslo: haslo123
[OK] Zalogowano. Witaj, jan_kowalski!

Wybierz opcje: 3
[i] Aktualna sesja: User{id=1, username='jan_kowalski', createdAt='2026-06-08T00:50:21'}

Wybierz opcje: 4
[OK] Wylogowano uzytkownika: jan_kowalski

Wybierz opcje: 0
Do zobaczenia!
```

## 8. Możliwe rozszerzenia

- **Hashowanie haseł** (np. SHA-256 / PBKDF2 / BCrypt) zamiast przechowywania
  w postaci jawnej – zmiana dotyczy jedynie klas `AuthService` i `UserDao`.
- **Testy jednostkowe** (JUnit 5) dla logiki logowania i działania Singletonów.
- Role użytkowników, blokada konta po nieudanych próbach, zmiana hasła.

---

> **Uwaga dydaktyczna:** w obecnej wersji hasła zapisywane są jako zwykły tekst,
> co jest świadomym uproszczeniem na potrzeby projektu skupionego na wzorcu
> Singleton. W zastosowaniu produkcyjnym hasła należy zawsze hashować.
