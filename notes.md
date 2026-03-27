# Testy wydajnościowe z Gatling

---

## Spis treści

1. [Czym są testy wydajnościowe i po co je pisać](#1-czym-są-testy-wydajnościowe-i-po-co-je-pisać)
2. [Kluczowe pojęcia i metryki](#2-kluczowe-pojęcia-i-metryki)
3. [Typy testów wydajnościowych](#3-typy-testów-wydajnościowych)
4. [Gatling — architektura i filozofia](#4-gatling--architektura-i-filozofia)
5. [Instalacja i konfiguracja projektu](#5-instalacja-i-konfiguracja-projektu)
6. [Simulation — punkt wejścia każdego testu](#6-simulation--punkt-wejścia-każdego-testu)
7. [Protokół HTTP — konfiguracja bazowa](#7-protokół-http--konfiguracja-bazowa)
8. [Scenario — opis zachowania użytkownika](#8-scenario--opis-zachowania-użytkownika)
9. [Sesja, Expression Language i Session API](#9-sesja-expression-language-i-session-api)
10. [Feeders — zewnętrzne dane testowe](#10-feeders--zewnętrzne-dane-testowe)
11. [Checks — weryfikacja i ekstrakcja danych z odpowiedzi](#11-checks--weryfikacja-i-ekstrakcja-danych-z-odpowiedzi)
12. [Profile wstrzykiwania użytkowników (Injection)](#12-profile-wstrzykiwania-użytkowników-injection)
13. [Assertions — warunki akceptacji testu](#13-assertions--warunki-akceptacji-testu)
14. [Dobre praktyki i organizacja kodu](#14-dobre-praktyki-i-organizacja-kodu)
15. [Typowe błędy i antywzorce](#15-typowe-błędy-i-antywzorce)

---

## 1. Czym są testy wydajnościowe i po co je pisać

Testy funkcjonalne odpowiadają na pytanie: *czy system robi to, co powinien?* Testy wydajnościowe odpowiadają na inne pytanie: *jak dobrze system to robi, gdy jednocześnie korzysta z niego wielu użytkowników?*

Testy funkcjonalne (Selenium, Cypress, JUnit) sprawdzają poprawność działania przy jednym zestawie danych, jednym użytkowniku i stabilnych warunkach. Testy wydajnościowe symulują setki lub tysiące użytkowników, mierzą czas odpowiedzi, przepustowość i stabilność pod obciążeniem.

Testy wydajnościowe są niezbędne na każdym etapie cyklu życia oprogramowania, nie tylko przed wdrożeniem produkcyjnym. Na etapie planowania pozwalają zdefiniować wymagania niefunkcjonalne — maksymalny czas odpowiedzi, liczbę obsługiwanych użytkowników, dostępność systemu. Na etapie implementacji umożliwiają wykrywanie regresji wydajnościowych po każdym mergowaniu zmian. Przed wdrożeniem dają pewność, że system utrzyma obciążenie produkcyjne. Na produkcji pomagają planować skalowanie i wykrywać degradację w czasie.

---

## 2. Kluczowe pojęcia i metryki

### Throughput (przepustowość)

Liczba operacji (żądań HTTP, transakcji) przetworzonych przez system w jednostce czasu, wyrażana zwykle jako RPS (requests per second) lub TPS (transactions per second). Throughput opisuje ogólną wydolność systemu — im wyższy, tym więcej pracy system wykonuje w danym czasie.

### Latency (opóźnienie)

Czas od wysłania żądania do momentu, gdy serwer zaczyna je przetwarzać. Wysoka latencja świadczy o przeciążeniu kolejek lub zasobów serwera, zanim jeszcze faktycznie zacznie odpowiadać na żądanie.

### Response time (czas odpowiedzi)

Całkowity czas od wysłania żądania przez użytkownika do otrzymania pełnej odpowiedzi. Obejmuje latency i czas przetwarzania. To wskaźnik bezpośrednio wpływający na doświadczenie użytkownika — jeśli odpowiedź zajmuje 8 sekund, użytkownik zwykle rezygnuje z oczekiwania.

### Percentyle

Percentyle pozwalają zrozumieć rozkład czasów odpowiedzi bez fałszowania obrazu przez wartości skrajne. Wartość P95 wynosząca 2 sekundy oznacza, że 95% żądań zostało obsłużonych w czasie nie dłuższym niż 2 sekundy — tylko 5% trwało dłużej. Percentyle są bardziej miarodajne niż średnia, która może być zaniżona przez dużą liczbę szybkich żądań i całkowicie ukrywać problemy z wolnymi.

Standardowo w branży stosuje się P50, P90, P95 i P99. P99 (99. percentyl) to najostrzejszy wskaźnik — mówi o doświadczeniu najbardziej niefortnych użytkowników.

### TPS vs RPS

RPS (requests per second) mierzy pojedyncze żądania HTTP. TPS (transactions per second) mierzy złożone transakcje biznesowe, np. cały proces: logowanie → dodanie do koszyka → płatność. TPS jest bardziej użyteczny przy testowaniu aplikacji e-commerce i systemów biznesowych.

### Wirtualni użytkownicy

Symulowani użytkownicy generowani przez narzędzie testowe. Każdy wirtualny użytkownik wykonuje zdefiniowany scenariusz, odwzorowując zachowanie prawdziwego człowieka — łącznie z pauzami między kliknięciami. Nie są to prawdziwe osoby ani przeglądarki; to lekkie wątki lub procesy asynchroniczne.

---

## 3. Typy testów wydajnościowych

### Load testing (test obciążeniowy)

Symuluje typowe, przewidywane obciążenie produkcyjne. Odpowiada na pytanie: *czy system działa poprawnie, gdy korzysta z niego oczekiwana liczba użytkowników jednocześnie?* Przykład: 500 użytkowników równocześnie przeglądających sklep internetowy. Wyniki pozwalają zweryfikować konfigurację infrastruktury, wykryć wąskie gardła (np. wolne zapytania do bazy danych) i potwierdzić, że czasy odpowiedzi spełniają wymagania SLA.

### Stress testing (test wytrzymałościowy)

Przekracza normalne obciążenie, aby sprawdzić, jak i kiedy system się psuje. Stopniowe zwiększanie liczby użytkowników aż do awarii lub znacznego spadku wydajności. Pozwala określić maksymalny próg wydolności i zrozumieć, czy system degraduje się łagodnie (wyższe czasy odpowiedzi, ale nadal działa), czy nagle przestaje odpowiadać.

### Spike testing (test skokowy)

Chwilowy, gwałtowny wzrost liczby użytkowników w bardzo krótkim czasie — symuluje flash sale, kampanię reklamową lub atak DDoS. Sprawdza, czy system radzi sobie z nagłym obciążeniem, czy mechanizmy autoskalowania reagują wystarczająco szybko i czy system wraca do normy po opadnięciu ruchu.

### Soak testing (test stabilności)

Utrzymanie umiarkowanego obciążenia przez wiele godzin lub dni. Wykrywa problemy, które ujawniają się dopiero po czasie: wycieki pamięci, wyczerpywanie puli połączeń do bazy danych, fragmentację pamięci, stopniową degradację wydajności. Niezbędny dla systemów krytycznych — bankowych, telekomunikacyjnych, medycznych.

---

## 4. Gatling — architektura i filozofia

Gatling to narzędzie do testów wydajnościowych oparte na modelu asynchronicznym. W odróżnieniu od Apache JMeter, który tworzy osobny wątek dla każdego wirtualnego użytkownika, Gatling wykorzystuje reaktywną architekturę opartą na bibliotece Akka i modelu aktorów. Dzięki temu jeden proces Gatlinga może symulować dziesiątki tysięcy użytkowników przy minimalnym zużyciu zasobów.

### Filozofia "test as code"

Testy Gatlinga to kod Java (lub Scala/Kotlin), przechowywany w repozytorium razem z kodem aplikacji. Oznacza to pełną historię zmian w Git, możliwość code review, reużywalność fragmentów kodu i łatwą integrację z CI/CD. To fundamentalna różnica w stosunku do narzędzi nagrywających testy w formacie GUI.

### Open Source vs Enterprise

Gatling Open Source jest w pełni funkcjonalny i bezpłatny — lokalnie tworzy, uruchamia i raportuje testy. Gatling Enterprise (dawniej FrontLine) rozszerza możliwości o rozproszone wykonywanie testów z wielu węzłów, zarządzanie przez GUI, integrację z systemami CI/CD na poziomie organizacji, monitoring w czasie rzeczywistym i historię testów.

### Główne komponenty

Każdy test Gatlinga składa się z czterech elementów:

**Simulation** to klasa Javy rozszerzająca `Simulation`. Zawiera pełną definicję testu i jest punktem wejścia dla Gatlinga. Jedna klasa = jeden test.

**Scenario** opisuje sekwencję kroków wykonywanych przez wirtualnego użytkownika: żądania HTTP, pauzy, pętle, warunki.

**Injection Profile** definiuje, ilu użytkowników, kiedy i w jakim tempie wchodzi do scenariusza.

**Checks** weryfikują odpowiedzi serwera i ekstrakcją dane do sesji użytkownika.

---

## 5. Instalacja i konfiguracja projektu

### Wymagania

- 64-bitowa Java (OpenJDK LTS 11–25). Zalecana dystrybucja: Azul JDK lub Eclipse Temurin.
- Maven 3.6.3+ lub Gradle 7.6+. Projekt startowy zawiera Maven Wrapper, więc nie trzeba instalować Mavena globalnie.

Przed uruchomieniem warto zweryfikować środowisko:

```bash
java -version
mvn -version
```

Jeśli zmienna `JAVA_HOME` wskazuje na niewłaściwą wersję JDK, Gatling zgłosi błąd `Unsupported major.minor version`.

### Projekt startowy

Gatling udostępnia oficjalny projekt startowy z Maven Wrapperem:

```bash
git clone https://github.com/gatling/gatling-maven-plugin-demo-java.git
cd gatling-maven-plugin-demo-java
./mvnw clean install
```

Alternatywnie można pobrać archiwum ZIP bezpośrednio ze strony projektu.

### Struktura projektu

```
src/
└── test/
    ├── java/
    │   └── com/example/
    │       └── MojaSimulation.java
    └── resources/
        ├── data/           ← pliki CSV, JSON z danymi testowymi
        ├── bodies/         ← szablony ciał żądań
        └── gatling.conf    ← konfiguracja Gatlinga
```

### Uruchamianie

```bash
# uruchomienie wszystkich symulacji (lub z wyborem interaktywnym)
./mvnw gatling:test

# uruchomienie konkretnej symulacji
./mvnw gatling:test -Dgatling.simulationClass=com.example.MojaSimulation

# przekazanie parametru (np. baseUrl)
./mvnw gatling:test -DbaseUrl=https://staging.example.com
```

Raport HTML trafia do katalogu `target/gatling/<nazwa>-<timestamp>/index.html`.

### Niezbędne importy

Każda klasa symulacji wymaga tych samych importów. Nie należy ich modyfikować ani "optymalizować" narzędziami IDE — można przez to zepsuć autowiring DSL:

```java
import io.gatling.javaapi.core.*;
import static io.gatling.javaapi.core.CoreDsl.*;

import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
```

---

## 6. Simulation — punkt wejścia każdego testu

`Simulation` to klasa bazowa, którą musi rozszerzać każda klasa testowa. Metoda `setUp()` jest jedynym obowiązkowym elementem — musi zostać wywołana dokładnie raz w konstruktorze klasy.

```java
public class MojaSimulation extends Simulation {

    // definicje protokołu, scenariuszy, feederów...

    {
        // blok inicjalizacyjny (konstruktor bezparametrowy)
        setUp(
            scenariusz.injectOpen(atOnceUsers(10))
        ).protocols(protokolHttp);
    }
}
```

Uwaga: nazwa klasy nie powinna zaczynać się od `Test`, bo niektóre narzędzia (np. Maven Surefire) traktują wtedy klasę jako test jednostkowy i próbują ją uruchomić w nieprawidłowy sposób.

### Globalnie konfiguracja pauzy

Pauzy można konfigurować globalnie dla całej symulacji. Pozwala to np. wyłączyć pauzy na czas debugowania lub użyć rozkładu normalnego zamiast stałych wartości:

```java
setUp(scn.injectOpen(atOnceUsers(1)))
    .disablePauses()                              // całkowite wyłączenie
    .constantPauses()                             // domyślne, wartości stałe
    .uniformPauses(0.5)                           // rozkład jednorodny ±50%
    .normalPausesWithPercentageDuration(20)       // rozkład normalny ±20%
    .exponentialPauses();                         // rozkład wykładniczy
```

### Throttling

Throttling pozwala ograniczyć łączną przepustowość (RPS) niezależnie od liczby wirtualnych użytkowników. Gatling wyłącza wtedy wszystkie pauzy i ogranicza tempo. Stosować ostrożnie — najlepiej wyłącznie do scenariuszy z jednym żądaniem na użytkownika:

```java
setUp(scn.injectOpen(constantUsersPerSec(100).during(Duration.ofMinutes(30))))
    .throttle(
        reachRps(100).in(10),          // osiągnij 100 RPS w 10 sekund
        holdFor(Duration.ofMinutes(1)), // utrzymaj przez 1 minutę
        jumpToRps(50),                  // skok do 50 RPS
        holdFor(Duration.ofHours(2))    // utrzymaj przez 2 godziny
    );
```

### Maksymalny czas trwania

`maxDuration` zatrzymuje test po określonym czasie, nawet jeśli wirtualni użytkownicy nadal działają. Przydatne gdy nie można dokładnie przewidzieć czasu trwania testu:

```java
setUp(scn.injectOpen(rampUsers(1000).during(Duration.ofMinutes(20))))
    .maxDuration(Duration.ofMinutes(10));
```

### Hooks before/after

Metody `before()` i `after()` pozwalają wykonać kod przed i po teście — np. przygotowanie danych, czyszczenie środowiska, powiadomienia. Nie można w nich używać DSL Gatlinga:

```java
@Override
public void before() {
    System.out.println("Test startuje...");
}

@Override
public void after() {
    System.out.println("Test zakończony.");
}
```

---

## 7. Protokół HTTP — konfiguracja bazowa

`HttpProtocolBuilder` definiuje wspólne ustawienia dla wszystkich żądań HTTP w symulacji. Konfiguruje się go raz i przekazuje do `setUp()`.

```java
HttpProtocolBuilder protokolHttp = http
    .baseUrl("https://api.example.com")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .acceptLanguageHeader("pl-PL,pl;q=0.9")
    .userAgentHeader("Gatling-Test/1.0");
```

Dzięki `baseUrl` wszystkie żądania w scenariuszu mogą używać ścieżek względnych: `.get("/users")` zamiast `.get("https://api.example.com/users")`.

**Dobra praktyka:** wartość `baseUrl` pobieraj ze zmiennej systemowej, nie koduj jej na stałe:

```java
private static final String BASE_URL =
    System.getProperty("baseUrl", "https://staging.example.com");
```

Uruchomienie z innym środowiskiem: `./mvnw gatling:test -DbaseUrl=https://prod.example.com`.

### Zarządzanie połączeniami

Domyślnie Gatling stosuje keep-alive (ponowne użycie połączeń TCP) i zarządza ciasteczkami osobno dla każdego wirtualnego użytkownika. Oto najważniejsze opcje:

```java
HttpProtocolBuilder protokolHttp = http
    .baseUrl("https://api.example.com")
    .disableKeepAlive()          // wyłącz keep-alive (tylko gdy serwer tego wymaga)
    .disableFollowRedirect()     // nie podążaj za przekierowaniami 3xx
    .disableCaches()             // wyłącz cache HTTP (testy "zimnych" odpowiedzi)
    .maxConnectionsPerHost(10);  // limit połączeń per host per użytkownik
```

### Przypisanie protokołu

Protokół można przypisać globalnie (do wszystkich scenariuszy) lub per scenariusz:

```java
// globalnie
setUp(scn1.injectOpen(...), scn2.injectOpen(...))
    .protocols(protokolHttp);

// per scenariusz — gdy różne scenariusze testują różne serwisy
setUp(
    scn1.injectOpen(...).protocols(protokolStaging),
    scn2.injectOpen(...).protocols(protokolProdukcja)
);
```

---

## 8. Scenario — opis zachowania użytkownika

Scenariusz (`ScenarioBuilder`) to logiczny opis, co robi jeden wirtualny użytkownik. Kroki wykonywane są sekwencyjnie.

```java
ScenarioBuilder scenariusz = scenario("Nazwa scenariusza")
    .exec(
        http("GET Strona glowna")
            .get("/")
            .check(status().is(200))
    )
    .pause(2)
    .exec(
        http("POST Logowanie")
            .post("/login")
            .body(StringBody("""{"user":"jan","pass":"tajne"}"""))
            .check(status().is(200))
    );
```

### exec

`exec` wykonuje akcję: żądanie HTTP lub manipulację sesją. Można przekazać wiele akcji naraz lub łączyć wywołania:

```java
exec(
    http("Krok 1").get("/a"),
    http("Krok 2").get("/b")
)
// identycznie jak:
exec(http("Krok 1").get("/a"))
    .exec(http("Krok 2").get("/b"))
```

`exec` przyjmuje też lambdę sesji — do manipulacji stanem wirtualnego użytkownika lub debugowania. Uwaga: nigdy nie wywołuj w tej lambdzie operacji blokujących (np. wywołań sieciowych), bo Gatling wykonuje ją w swoim wątku asynchronicznym:

```java
exec(session -> {
    System.out.println("[DEBUG] Sesja: " + session); // tylko do debugowania!
    return session.set("nowaZmienna", "wartosc");
})
```

### pause

Pauza odwzorowuje naturalne "myślenie" użytkownika między kliknięciami. Bez pauz test nie jest realistyczny — każdy użytkownik wysyła żądania tak szybko, jak pozwala sieć, co generuje sztuczne, nierealne obciążenie.

```java
pause(2)                                                   // stałe 2 sekundy
pause(Duration.ofMillis(500))                              // 500 milisekund
pause(1, 5)                                                // losowo 1–5 sekund
pause(Duration.ofSeconds(1), Duration.ofSeconds(3))        // losowo 1–3 sekundy
pause("#{czas}")                                           // z wartości w sesji
```

### pace

`pace` kontroluje częstotliwość iteracji — użytkownik wykonuje pętlę dokładnie raz na zadany czas, niezależnie od tego, ile zajął poprzedni krok:

```java
forever().on(
    pace(5)  // iteracja co 5 sekund niezależnie od czasu wykonania
        .exec(http("Odswiezenie").get("/status"))
)
```

### Pętle

**repeat** — stała liczba iteracji:

```java
repeat(5).on(
    http("Powtarzany krok").get("/item")
)

// z licznikiem dostępnym w sesji
repeat(5, "i").on(
    http("Krok #{i}").get("/item?nr=#{i}")
)
```

**foreach** — iteracja po elementach listy zapisanej w sesji:

```java
foreach("#{listaElementow}", "element").on(
    http("Krok dla #{element}").get("/item/#{element}")
)
```

**during** — pętla przez określony czas:

```java
during(Duration.ofMinutes(2)).on(
    http("Cykliczne odpytywanie").get("/status")
)
```

**asLongAs** — pętla dopóki warunek jest prawdziwy:

```java
asLongAs(session -> session.getInt("licznik") < 10).on(
    exec(http("Krok").get("/"))
        .exec(session -> session.set("licznik", session.getInt("licznik") + 1))
)
```

**forever** — pętla nieskończona (zatrzymywana przez `maxDuration` lub `exitHereIf`):

```java
forever().on(
    exec(http("Ping").get("/health"))
        .pause(10)
)
```

### Warunki

**doIf** — wykonaj blok tylko gdy warunek spełniony:

```java
doIf(session -> "admin".equals(session.getString("rola"))).then(
    exec(http("Panel admina").get("/admin"))
)
```

**doIfOrElse** — rozgałęzienie z alternatywą:

```java
doIfOrElse("#{zalogowany}").then(
    exec(http("Strona uzytkownika").get("/profile"))
).orElse(
    exec(http("Strona logowania").get("/login"))
)
```

**doIfEquals** — skrót dla porównania wartości:

```java
doIfEquals("#{status}", "aktywny").then(
    exec(http("Strona aktywna").get("/active"))
)
```

**randomSwitch** — losowe ścieżki z określonym prawdopodobieństwem (model łańcuchów Markowa). Idealne do modelowania mieszanego ruchu: część użytkowników przegląda, część kupuje, część rezygnuje:

```java
randomSwitch().on(
    percent(70.0).then(exec(http("Przegladanie").get("/products"))),
    percent(20.0).then(exec(http("Zakup").post("/checkout"))),
    percent(10.0).then(exec(http("Wyszukiwanie").get("/search")))
)
```

### Obsługa błędów

**tryMax** — ponów cały blok maksymalnie N razy przy błędzie. Wszystkie nieudane próby są logowane:

```java
tryMax(3).on(
    exec(http("Niestabilny endpoint").get("/flaky"))
)
```

**exitBlockOnFail** — przerwij blok przy pierwszym błędzie, ale kontynuuj scenariusz:

```java
exitBlockOnFail().on(
    exec(http("Krok krytyczny").get("/critical"))
)
```

**exitHereIfFailed** — zakończ scenariusz dla tego użytkownika jeśli poprzedni krok się nie powiódł:

```java
tryMax(2).on(exec(http("Logowanie").post("/login").check(status().is(200))))
    .exitHereIfFailed()
    .exec(/* dalsze kroki tylko gdy logowanie się powiodło */)
```

**exitHereIf** — zakończ warunkowo:

```java
exitHereIf(session -> session.getBoolean("anulowano"))
```

### Grupowanie kroków

`group` tworzy logiczną grupę kroków widoczną w raporcie jako oddzielna kategoria z własnym czasem odpowiedzi. Przydatne do modelowania procesów biznesowych (np. "Rejestracja", "Zakup"):

```java
group("Proces zakupu").on(
    exec(http("Koszyk").get("/cart")),
    pause(1),
    exec(http("Płatność").post("/payment")),
    pause(1),
    exec(http("Potwierdzenie").get("/confirmation"))
)
```

---

## 9. Sesja, Expression Language i Session API

Sesja (Session) to obiekt przechowujący dane konkretnego wirtualnego użytkownika. Każdy użytkownik ma własną, izolowaną sesję. Dane trafiają do sesji z trzech źródeł: z feedera (`.feed()`), z odpowiedzi serwera (`.check(...).saveAs("klucz")`), oraz bezpośrednio przez lambdę sesji.

### Expression Language

Expression Language (EL) to specjalna składnia `#{nazwaZmiennej}` pozwalająca odwoływać się do zmiennych sesji w miejscach przyjmujących wartości dynamiczne:

```java
http("GET Uzytkownik").get("/users/#{userId}")
http("Krok #{numer}").get("/step/#{numer}")
.body(StringBody("""{"id": #{id}, "name": "#{name}"}"""))
.queryParam("page", "#{strona}")
```

EL jest ewaluowane leniwie — dopiero w momencie wykonania kroku przez wirtualnego użytkownika, nie podczas budowania scenariusza. Błąd w wyrażeniu EL (np. odwołanie do nieistniejącej zmiennej) pojawia się w raporcie jako błąd żądania, a nie błąd kompilacji.

Gatling EL obsługuje też podstawowe operacje: `#{zmienna.upperCase()}`, `#{zmienna.lowerCase()}`, `#{randomInt(1,100)}`.

### Session API

Sesja jest niemutowalna (immutable) — każda modyfikacja zwraca nową instancję:

```java
exec(session -> {
    // Odczyt wartości
    int id      = session.getInt("userId");
    String name = session.getString("userName");
    boolean ok  = session.getBoolean("isLoggedIn");

    // Zapis nowej wartości
    Session zaktualizowana = session.set("fullName", name + " #" + id);

    // Usunięcie wartości
    Session wyczyszczona = zaktualizowana.remove("tempToken");

    // Sprawdzenie czy klucz istnieje
    boolean exists = session.contains("userId");

    return wyczyszczona;
})
```

### Funkcje sesji jako alternatywa dla EL

Gdy EL nie wystarczy — np. gdy potrzebna jest konwersja typów lub złożona logika — użyj lambdy sesji:

```java
.queryParam("limit", session -> {
    int bazowy = session.getInt("bazowy");
    return bazowy * 2 + 10;
})
```

---

## 10. Feeders — zewnętrzne dane testowe

Feeder to źródło danych wstrzykiwanych do sesji wirtualnych użytkowników. Każde wywołanie `.feed(feeder)` pobiera jeden rekord i zapisuje jego pola jako atrybuty sesji.

```java
ScenarioBuilder scn = scenario("Test z feederem")
    .feed(feeder)            // pobierz rekord, zapisz pola do sesji
    .exec(
        http("GET /users/#{userId}")  // użyj pola z feedera
            .get("/users/#{userId}")
    );
```

### Feedery plikowe

Pliki umieszczaj w `src/test/resources/data/`. Używaj ścieżki relatywnej od tego katalogu, nie bezwzględnej ścieżki do systemu plików.

Przykładowy plik `data/uzytkownicy.csv`:
```
login,haslo,rola
jan.kowalski@example.com,haslo123,USER
anna.nowak@example.com,pass456,ADMIN
```

```java
FeederBuilder.FileBased<String> feederCsv = csv("data/uzytkownicy.csv").circular();
FeederBuilder.FileBased<String> feederTsv = tsv("data/dane.tsv").random();
FeederBuilder.FileBased<Object> feederJson = jsonFile("data/produkty.json").queue();
```

Plik JSON musi być tablicą obiektów:
```json
[
  {"productId": 1, "name": "Laptop"},
  {"productId": 2, "name": "Monitor"}
]
```

### Feedery w pamięci

```java
FeederBuilder<Object> feederWPamieci = listFeeder(List.of(
    Map.of("kategoria", "electronics", "limit", 10),
    Map.of("kategoria", "books",       "limit", 20),
    Map.of("kategoria", "clothing",    "limit", 15)
)).random();
```

### Feeder dynamiczny (własny generator)

`Iterator` generujący dane w locie — idealny gdy dane muszą być unikalne i niemożliwe do wygenerowania z góry:

```java
Iterator<Map<String, Object>> feederDynamiczny =
    Stream.generate((Supplier<Map<String, Object>>) () ->
        Map.of(
            "unikalnyId",      UUID.randomUUID().toString(),
            "znacznikCzasu",   System.currentTimeMillis()
        )
    ).iterator();
```

### Strategie

| Strategia | Zachowanie | Kiedy używać |
|-----------|------------|-------------|
| `queue()` | domyślna; rekordy zużywane po kolei; crash gdy dane się wyczerpią | unikalne dane (loginy, nr zamówień) w kontrolowanej liczbie |
| `shuffle()` | jak queue, ale w losowej kolejności | unikalne dane, losowa kolejność |
| `random()` | losowy rekord, możliwe powtórzenia, dane nigdy się nie wyczerpią | dane mogą się powtarzać (słowa kluczowe, kategorie) |
| `circular()` | po wyczerpaniu zaczyna od początku, możliwe powtórzenia | dane mogą się powtarzać, naturalna kolejność |

**Kiedy używać której strategii:** jeśli dane są unikalne i nie mogą się powtarzać (np. każdy użytkownik musi zalogować się na inne konto), użyj `queue()` lub `shuffle()`. Pamiętaj, że `queue()` zakończy się crashem jeśli wirtualnych użytkowników jest więcej niż rekordów w feedzie — dobierz rozmiar pliku CSV do planowanej liczby użytkowników. Jeśli dane mogą się powtarzać, `circular()` lub `random()` są bezpieczniejsze.

### Pobieranie wielu rekordów naraz

```java
feed(feeder, 3)                              // zawsze 3 rekordy
feed(feeder, "#{liczbaRekordow}")            // z sesji
feed(feeder, session -> session.getInt("n")) // z funkcji
```

### Transformacja rekordów

```java
csv("dane.csv").transform((klucz, wartosc) ->
    klucz.equals("wiek") ? Integer.valueOf(wartosc) : wartosc
)
```

---

## 11. Checks — weryfikacja i ekstrakcja danych z odpowiedzi

Check spełnia dwie role jednocześnie: weryfikuje poprawność odpowiedzi i ekstrahuje z niej dane do sesji. Jeśli check się nie powiedzie, żądanie zostanie oznaczone jako FAILED w raporcie — nawet jeśli serwer zwrócił status HTTP 200.

Łańcuch check składa się z kroków: **typ** → **ekstrakcja** → **transformacja** → **walidacja** → **zapis**.

### Typy checks

**Status HTTP** — najważniejszy, zawsze go dodawaj:

```java
.check(status().is(200))
.check(status().not(404), status().not(500))
.check(status().in(200, 201, 202))
```

**Czas odpowiedzi** — do weryfikacji SLA na poziomie pojedynczego żądania:

```java
.check(responseTimeInMillis().lte(2000))
```

**jsonPath** — wyrażenie w stylu XPath dla JSON. Nieformalna specyfikacja — zachowanie może różnić się między implementacjami:

```java
.check(jsonPath("$.id").ofInt().saveAs("userId"))
.check(jsonPath("$[0].name").is("Jan"))
.check(jsonPath("$.items").ofList().saveAs("lista"))
```

**jmesPath** — alternatywa dla jsonPath z formalną specyfikacją i zestawem testów zgodności. Wyrażenie przetestowane w [online evaluatorze](https://jmespath.org/) zadziała identycznie w Gatlingu:

```java
.check(jmesPath("id").ofInt().saveAs("userId"))
.check(jmesPath("user.name").is("Jan"))
.check(jmesPath("items[0].price").ofDouble().gt(0.0))
```

**substring** — sprawdzenie obecności podciągu w treści odpowiedzi. Wydajniejsze niż regex dla prostych przypadków:

```java
.check(substring("success").exists())
.check(substring("error").notExists())
```

**regex** — wyrażenie regularne na treści odpowiedzi:

```java
.check(regex("orderId=(\\d+)").saveAs("orderId"))
.check(regex("error").notExists())
```

**bodyString** — cała treść odpowiedzi jako string:

```java
.check(bodyString().is("{\"status\":\"ok\"}"))
```

### Ekstrakcja

`find()` — domyślne, pierwsze lub jedyne dopasowanie. Można pominąć, Gatling stosuje je niejawnie.

`find(n)` — n-te dopasowanie (indeksowanie od 0):

```java
.check(jsonPath("$.items[*].id").find(2).saveAs("trzeciId"))
```

`findAll()` — wszystkie dopasowania jako lista:

```java
.check(jsonPath("$.items[*].id").findAll().saveAs("wszystkieId"))
```

`findRandom()` — losowe dopasowanie:

```java
.check(jsonPath("$.items[*].id").findRandom().saveAs("losowyId"))
```

`count()` — liczba dopasowań:

```java
.check(regex("item").count().is(5))
```

### Walidacja

`is(wartosc)` — sprawdza równość. Przyjmuje wartość statyczną, EL lub lambdę:

```java
.check(jmesPath("status").is("active"))
.check(jmesPath("userId").is(session -> session.getInt("expectedId").toString()))
```

`not(wartosc)` — sprawdza nierówność.

`exists()` — sprawdza obecność (domyślne zachowanie gdy nie podano walidatora).

`notExists()` — sprawdza brak wartości.

`in(wartosc1, wartosc2, ...)` — sprawdza przynależność do zbioru.

`optional()` — pole może nie istnieć; check nie failuje gdy go brak, nie wykonuje też `saveAs`.

### Transformacja

`transform` — przekształca wyekstrahowaną wartość przed walidacją. Wykonywana tylko gdy ekstrakcja zakończyła się sukcesem:

```java
.check(
    jsonPath("$.price")
        .ofDouble()
        .transform(cena -> cena * 1.23) // przelicz cenę netto na brutto
        .saveAs("cenaBrutto")
)
```

`transformWithSession` — transformacja z dostępem do sesji:

```java
.check(
    jmesPath("discount")
        .ofDouble()
        .transformWithSession((rabat, session) ->
            session.getDouble("cenaBase") * (1 - rabat)
        )
        .saveAs("cenaKoncowa")
)
```

### Zapis do sesji

`.saveAs("klucz")` — zapisuje wyekstrahowaną i zwalidowaną wartość w sesji. Zapis następuje tylko gdy check jest pomyślny:

```java
.check(
    jmesPath("token").saveAs("authToken")
)
// w kolejnych krokach: .header("Authorization", "Bearer #{authToken}")
```

### Warunkowe sprawdzanie

`checkIf` pozwala wykonać check tylko gdy spełniony jest dodatkowy warunek:

```java
.checkIf(session -> "premium".equals(session.getString("plan"))).then(
    jmesPath("premiumFeatures").exists()
)
.checkIf("#{sprawdzajCeny}").then(
    jmesPath("price").ofDouble().gt(0.0)
)
```

### Nadawanie nazw

`.name()` pozwala ustawić własny komunikat błędu dla checku — szczególnie przydatne w CI/CD gdzie czytelność logów jest kluczowa:

```java
.check(
    jmesPath("orderId").exists()
        .name("Odpowiedź musi zawierać pole orderId")
)
```

---

## 12. Profile wstrzykiwania użytkowników (Injection)

Profile wstrzykiwania określają, ilu użytkowników, kiedy i w jakim tempie wchodzi do scenariusza. To jeden z najważniejszych parametrów definiujących charakter testu.

### Model otwarty vs zamknięty

**Model otwarty** (`injectOpen`) modeluje napływ nowych użytkowników niezależnie od tego, czy poprzedni skończyli. Nowi użytkownicy przychodzą w stałym tempie. Odpowiada rzeczywistości aplikacji webowych i REST API — użytkownicy odwiedzają stronę niezależnie od tego, ile osób jest aktualnie na stronie.

**Model zamknięty** (`injectClosed`) utrzymuje stałą liczbę aktywnych sesji. Nowy użytkownik startuje dopiero gdy poprzedni skończy. Odpowiada systemom z ograniczoną pulą zasobów, np. bazom danych z limitowaną liczbą połączeń.

Wybór modelu powinien odzwierciedlać rzeczywistość testowanego systemu — użycie złego modelu daje fałszywe wyniki.

### Model otwarty — bloki konstrukcyjne

`nothingFor(czas)` — pauza bez generowania ruchu. Przydatna do rozdzielenia faz:

```java
nothingFor(Duration.ofSeconds(5))
```

`atOnceUsers(n)` — natychmiastowe uruchomienie n użytkowników. Do testów weryfikacyjnych (smoke test):

```java
atOnceUsers(10)
```

`rampUsers(n).during(czas)` — stopniowe zwiększanie liczby użytkowników przez określony czas. Symuluje naturalne wypełnianie się systemu ruchem:

```java
rampUsers(100).during(Duration.ofSeconds(30))
```

`constantUsersPerSec(n).during(czas)` — stałe tempo nowych użytkowników na sekundę. Użytkownicy mogą być wstrzykiwani regularnie lub losowo:

```java
constantUsersPerSec(20).during(Duration.ofMinutes(5))
constantUsersPerSec(20).during(Duration.ofMinutes(5)).randomized()
```

`rampUsersPerSec(n1).to(n2).during(czas)` — liniowe zwiększanie tempa od n1 do n2:

```java
rampUsersPerSec(10).to(100).during(Duration.ofMinutes(2))
```

`stressPeakUsers(n).during(czas)` — gwałtowny wzrost użytkowników według krzywej Heaviside'a. Do testów spike:

```java
stressPeakUsers(1000).during(Duration.ofSeconds(20))
```

### Łączenie bloków (sekwencyjnie)

Bloki wykonywane są jeden po drugim. Kolejny zaczyna się gdy wszyscy użytkownicy z poprzedniego bloku już startowali (nie skończyły — startowali):

```java
scenariusz.injectOpen(
    nothingFor(Duration.ofSeconds(5)),          // odczekaj 5 sekund
    rampUsers(50).during(Duration.ofSeconds(30)),// stopniowo do 50
    constantUsersPerSec(50).during(Duration.ofMinutes(5)) // utrzymaj 5 minut
)
```

### Wzorzec schodkowy (capacity test)

Do stopniowego odkrywania limitu wydolności systemu. `incrementUsersPerSec` generuje profil schodkowy automatycznie:

```java
scenariusz.injectOpen(
    incrementUsersPerSec(10.0)     // zwiększaj o 10 req/s
        .times(5)                   // przez 5 poziomów: 10, 20, 30, 40, 50 req/s
        .eachLevelLasting(Duration.ofSeconds(30))
        .separatedByRampsLasting(Duration.ofSeconds(10))
        .startingFrom(10.0)
)
```

### Model zamknięty — bloki konstrukcyjne

`constantConcurrentUsers(n).during(czas)` — utrzymaj dokładnie n aktywnych użytkowników przez cały czas:

```java
constantConcurrentUsers(50).during(Duration.ofMinutes(5))
```

`rampConcurrentUsers(n1).to(n2).during(czas)` — liniowe zwiększanie liczby aktywnych użytkowników:

```java
rampConcurrentUsers(10).to(100).during(Duration.ofMinutes(3))
```

Podobnie jak dla modelu otwartego, dostępny jest `incrementConcurrentUsers` do automatycznego generowania profilu schodkowego.

### Wiele scenariuszy równolegle

Scenariusze przekazane do `setUp()` uruchamiają się równocześnie. Modeluje to realistyczny miks ruchu (np. 70% użytkowników przegląda, 20% kupuje, 10% przeszukuje):

```java
setUp(
    scenariuszPrzegladania.injectOpen(constantUsersPerSec(70).during(Duration.ofMinutes(5))),
    scenariuszZakupu.injectOpen(constantUsersPerSec(20).during(Duration.ofMinutes(5))),
    scenariuszWyszukiwania.injectOpen(constantUsersPerSec(10).during(Duration.ofMinutes(5)))
).protocols(protokolHttp);
```

### Scenariusze sekwencyjne (andThen)

`andThen` uruchamia następny scenariusz dopiero gdy wszyscy użytkownicy poprzedniego zakończyli działanie. Przydatne np. gdy najpierw trzeba pobrać token autentykacji (1 użytkownik), a potem właściwy test:

```java
setUp(
    inicjalizacja.injectOpen(atOnceUsers(1)).noShard()
        .andThen(
            glownyTest.injectOpen(rampUsers(100).during(Duration.ofSeconds(30)))
        )
)
```

`noShard()` zapobiega rozdzielaniu profilu wstrzykiwania między węzły w trybie rozproszonym — przydatne gdy inicjalizacja powinna odbyć się dokładnie raz.

---

## 13. Assertions — warunki akceptacji testu

Assertions (asercje) są ewaluowane po zakończeniu całej symulacji. Jeśli choć jedna asercja się nie powiedzie, test kończy się kodem wyjścia 1, co pozwala CI/CD (Jenkins, GitHub Actions, GitLab CI) automatycznie wykryć regresję wydajnościową i zablokować wdrożenie.

```java
setUp(scn.injectOpen(injectionProfile))
    .protocols(protokolHttp)
    .assertions(
        global().responseTime().max().lt(5000),
        global().responseTime().percentile(95.0).lt(2000),
        global().failedRequests().percent().lte(1.0)
    );
```

### Zakresy

`global()` — statystyki dla wszystkich żądań łącznie.

`forAll()` — statystyki dla każdego żądania z osobna (rygorystyczne; jedno wolne żądanie failuje cały test).

`details("NazwaZadania")` — statystyki konkretnego żądania lub grupy:

```java
details("POST Logowanie").responseTime().percentile(99.0).lt(3000)
details("Proces zakupu", "Płatność").failedRequests().percent().is(0.0)
```

### Dostępne statystyki

`responseTime()` — czas odpowiedzi w milisekundach.

`allRequests()` / `failedRequests()` / `successfulRequests()` — liczba żądań.

`requestsPerSec()` — tempo żądań na sekundę.

### Metryki dla responseTime

`min()` / `max()` / `mean()` / `stdDev()` — wartości ekstremalne, średnia, odchylenie standardowe.

`percentile(wartosc)` — konkretny percentyl (0–100):

```java
global().responseTime().percentile(95.0).lt(2000)
global().responseTime().percentile(99.0).lt(5000)
```

`percentile1()` / `percentile2()` / `percentile3()` / `percentile4()` — predefiniowane percentyle konfigurowane w `gatling.conf` (domyślnie: P50, P75, P95, P99).

### Metryki dla liczby żądań

`percent()` — procent (0–100):

```java
global().failedRequests().percent().lte(1.0)
```

`count()` — liczba bezwzględna:

```java
global().failedRequests().count().is(0L)
```

### Warunki

`lt(n)` / `lte(n)` / `gt(n)` / `gte(n)` — mniejszy / mniejszy lub równy / większy / większy lub równy.

`between(min, max)` — wartość w przedziale domkniętym.

`around(wartosc, margines)` — wartość w otoczeniu punktu.

`is(wartosc)` — dokładna równość.

`in(wartosc1, wartosc2)` — przynależność do zbioru.

### Typowe progi SLA

Branżowe progi jako punkt wyjścia do rozmowy z zespołem i SRE:

```java
.assertions(
    global().responseTime().max().lt(5000),          // max < 5s (twarda granica UX)
    global().responseTime().percentile(95.0).lt(2000),// P95 < 2s (standard SLA)
    global().responseTime().percentile(99.0).lt(4000),// P99 < 4s
    global().responseTime().mean().lt(800),           // średnia < 800ms
    global().failedRequests().percent().lte(1.0),     // błędy < 1%
    global().requestsPerSec().gte(100.0)              // min 100 RPS
)
```

---

## 14. Dobre praktyki i organizacja kodu

### Struktura projektu dla dojrzałego projektu

```
src/test/
├── java/pl/mojafirma/testy/
│   ├── simulations/
│   │   ├── SmokeTestSimulation.java     ← 1 użytkownik, weryfikacja
│   │   ├── LoadTestSimulation.java      ← pełny test obciążeniowy
│   │   ├── StressTestSimulation.java    ← test wytrzymałościowy
│   │   └── SoakTestSimulation.java      ← test stabilności (długi)
│   ├── scenarios/
│   │   ├── AuthScenarios.java           ← logowanie, wylogowanie
│   │   ├── ProductScenarios.java        ← przeglądanie produktów
│   │   └── CheckoutScenarios.java       ← proces zakupu
│   ├── config/
│   │   ├── HttpProtocolConfig.java      ← wspólna konfiguracja HTTP
│   │   └── EnvironmentConfig.java       ← zmienne środowiskowe
│   └── data/
│       └── TestDataFactory.java         ← fabryki feederów
└── resources/
    ├── data/
    │   ├── users.csv
    │   └── products.json
    ├── bodies/
    │   └── createOrder.json             ← szablony ciał żądań
    └── gatling.conf
```

### Wzorzec "Page Object" dla API

Zamiast definiować żądania inline w scenariuszach, twórz klasy reprezentujące zasoby lub endpointy. Scenariusze stają się przez to czytelne i reużywalne:

```java
// ProductsApi.java
public class ProductsApi {
    public static HttpRequestActionBuilder getAll() {
        return http("GET Lista produktow")
            .get("/products")
            .check(status().is(200));
    }

    public static HttpRequestActionBuilder getById() {
        return http("GET Produkt")
            .get("/products/#{productId}")
            .check(status().is(200))
            .check(jmesPath("id").ofInt().is(session -> session.getInt("productId")));
    }

    public static HttpRequestActionBuilder create() {
        return http("POST Nowy produkt")
            .post("/products")
            .body(ElFileBody("bodies/createProduct.json"))
            .check(status().is(201))
            .check(jmesPath("id").ofInt().saveAs("newProductId"));
    }
}

// W scenariuszu:
ScenarioBuilder scn = scenario("Zarzadzanie produktami")
    .exec(ProductsApi.getAll())
    .pause(1, 3)
    .exec(ProductsApi.getById())
    .pause(1)
    .exec(ProductsApi.create());
```

### Wzorzec "Profile-per-file"

Współdziel scenariusze między różnymi typami testów, zmieniając tylko profile wstrzykiwania:

```java
// SmokeTestSimulation.java
public class SmokeTestSimulation extends Simulation {
    {
        setUp(Scenarios.wszystkie().injectOpen(atOnceUsers(1)))
            .protocols(HttpProtocolConfig.domyslny())
            .assertions(global().failedRequests().count().is(0L));
    }
}

// LoadTestSimulation.java
public class LoadTestSimulation extends Simulation {
    {
        setUp(Scenarios.wszystkie().injectOpen(
            rampUsers(50).during(Duration.ofSeconds(30)),
            constantUsersPerSec(50).during(Duration.ofMinutes(5))
        ))
        .protocols(HttpProtocolConfig.domyslny())
        .assertions(
            global().responseTime().percentile(95.0).lt(2000),
            global().failedRequests().percent().lte(1.0)
        );
    }
}
```

### Konfiguracja gatling.conf

Plik `src/test/resources/gatling.conf` pozwala zmieniać konfigurację bez modyfikacji kodu:

```hocon
gatling {
  core {
    outputDirectoryBaseName = "wyniki"
    encoding = "utf-8"
    runDescription = "Test obciążeniowy — staging"
  }
  http {
    maxConnectionsPerHost = 6
    requestTimeout = 60000          # ms
    pooledConnectionIdleTimeout = 60000
  }
  data {
    writers = [console, file]
    console {
      light = false
    }
  }
}
```

### Nazewnictwo

Czytelne nazwy żądań są niezbędne dla zrozumienia raportów i logów:

- Scenariusze: opisują rolę użytkownika — `"Uzytkownik przegladajacy"`, `"Administrator"`, `"Uzytkownik premium"`
- Żądania HTTP: opisują operację — `"GET Lista produktow"`, `"POST Logowanie"`, `"PUT Aktualizacja profilu"`. Unikaj nazw technicznych jak `"Request_1"` czy `"Step3"`
- Grupy: opisują proces biznesowy — `"Proces zakupu"`, `"Rejestracja konta"`, `"Wyszukiwanie"`
- Klasy symulacji: PascalCase z opisem testu — `LoadTestSimulation`, `ApiGatewayStressTest`
- Feedery: opisują źródło — `feederUzytkownikow`, `feederProduktow`

### Zarządzanie konfiguracją środowiskową

```java
public class EnvironmentConfig {
    public static String baseUrl() {
        return System.getProperty("baseUrl",
            System.getenv().getOrDefault("BASE_URL", "https://staging.example.com"));
    }

    public static int maxUsers() {
        return Integer.parseInt(System.getProperty("maxUsers", "100"));
    }

    public static Duration testDuration() {
        int minuty = Integer.parseInt(System.getProperty("durationMinutes", "5"));
        return Duration.ofMinutes(minuty);
    }
}
```

Uruchamianie z różnymi konfiguracjami:

```bash
# test smoke
./mvnw gatling:test -Dgatling.simulationClass=...SmokeTestSimulation

# test obciążeniowy na stagingu
./mvnw gatling:test -Dgatling.simulationClass=...LoadTestSimulation \
  -DbaseUrl=https://staging.example.com -DmaxUsers=200

# test na produkcji z ograniczeniami
./mvnw gatling:test -DbaseUrl=https://prod.example.com \
  -DmaxUsers=50 -DdurationMinutes=2
```

---

## 15. Typowe błędy i antywzorce

### Brak pauz między żądaniami

Problem: każdy wirtualny użytkownik wysyła żądania tak szybko jak pozwala sieć, generując nienaturalne, skokowe obciążenie zupełnie niepodobne do ruchu produkcyjnego. Test mierzy coś innego niż rzeczywistość.

Rozwiązanie: dodaj `.pause()` między krokami odzwierciedlające realny "czas myślenia" użytkownika. Typowo 1–5 sekund. Użyj zakresu, nie wartości stałej — ludzie nie są regularni.

### Użycie Thread.sleep() zamiast pause()

Problem: `Thread.sleep()` blokuje wątek wykonawczy, niszcząc asynchroniczny model Gatlinga. Silnik przestaje obsługiwać innych wirtualnych użytkowników przez czas snu.

Rozwiązanie: zawsze używaj `.pause()` z Gatling DSL.

### Kodowanie URL i danych na stałe

Problem: test działa tylko dla jednego środowiska, dane uwierzytelniające trafiają do repozytorium.

Rozwiązanie: `System.getProperty()` dla URL i parametrów, feedery z pliku dla danych użytkowników. Warto rozważyć `.gitignore` dla pliku CSV z danymi wrażliwymi.

### Testowanie wyłącznie happy path

Problem: system może zachowywać się zupełnie inaczej gdy obsługuje błędy (np. 404, 429, 503). Nie testujesz ścieżek, po których realnie chodzą użytkownicy.

Rozwiązanie: uwzględnij w teście nieprawidłowe żądania, scenariusze wyszukiwania nieistniejących zasobów, obsługę limitów.

### Jeden scenariusz dla wszystkich użytkowników

Problem: rzeczywisty ruch to zawsze miks różnych zachowań. Jeden scenariusz "wszystkich robiących to samo" nie odzwierciedla produkcji.

Rozwiązanie: osobne scenariusze dla różnych ról z `randomSwitch` lub wieloma scenariuszami w `setUp()` z odpowiednimi proporcjami.

### Test bez assertions

Problem: test zawsze "przechodzi" w CI/CD, niezależnie od wyników. Regresja wydajnościowa pozostaje niezauważona.

Rozwiązanie: zawsze definiuj `.assertions()` z progami SLA uzgodnionymi z zespołem produktowym.

### Uruchamianie testów z maszyny dewelopera

Problem: laptop dzieli zasoby CPU/sieć z IDE i innymi aplikacjami. Wyniki są niestabilne i niemożliwe do porównania między uruchomieniami.

Rozwiązanie: dedykowana maszyna do testów, najlepiej w tej samej sieci co testowany serwer. W CI/CD: osobny runner z dedykowanymi zasobami.

### Ignorowanie poziomu systemu operacyjnego

Gatling może potrzebować wielu otwartych połączeń jednocześnie. Domyślny limit otwartych plików w Linuksie (1024) może być za mały. Przed dużymi testami sprawdź i zwiększ limit:

```bash
# sprawdzenie limitu
ulimit -n

# tymczasowe zwiększenie
ulimit -n 65536
```
