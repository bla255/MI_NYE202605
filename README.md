# Connect4 Java AI – Minimax, JavaFX, MySQL

Egy teljes értékű Connect4 (négy a sorban) játék Java-ban,  
Minimax + Alpha–Beta alapú mesterséges intelligenciával, JavaFX grafikus felülettel és MySQL perzisztenciával.  

## Fő funkciók

- **Kétjátékos mód** (ember–ember)
- **Ember vs. Gép** – Minimax + Alpha–Beta vágás
- **Állítható nehézség** (mélység, heurisztika)
- **JavaFX GUI** – FXML nélküli, programozott felület
- **JSON mentés/betöltés** (lokális állapotmentés)
- **MySQL adatbázis**:
  - felhasználók (`users`)
  - lejátszott játékok (`games`)
- **Unit tesztek** + **JaCoCo branch coverage** riport

---

## Projekt felépítése

A forráskód a `src/main/java` alatt található, a csomagstruktúra:

- `model` – játéklogika, állapot
- `view` – JavaFX nézet
- `controller` / `service` – játékmenet, perzisztencia
- `ai` – Minimax, heurisztika
- `repository` – adatbázis elérés (JDBC)

A tesztek a `src/test/java` alatt találhatók.

---

## Architektúra – MVC + Repository + AI

### Model réteg

- **`Board`**
  - A játéktábla reprezentációja.
  - Belsőleg egy 2D tömb: `Player[][]`.
  - Felel:
    - korong lerakása adott oszlopba,
    - oszlop telítettség vizsgálata,
    - győzelem / döntetlen ellenőrzése.

- **`Player`**
  - Enum: pl. `RED`, `YELLOW`, `EMPTY`.
  - Jelöli az aktuális játékost és a mezők állapotát.

- **`GameState`**
  - Tartalmazza:
    - az aktuális `Board`-ot,
    - a soron lévő `Player`-t,
    - opcionálisan a játékmódot, nehézséget stb.
  - Ez az objektum az, amit a GUI, az AI és a perzisztencia is használ.

- **`Move`**
  - Egy lépés reprezentációja (pl. oszlop index, játékos).

---

### View réteg (JavaFX)

- **`Connect4App`**
  - A JavaFX `Application` belépési pont.
  - Létrehozza és összedrótozza:
    - a `GameManager`-t,
    - a `BoardView`-t,
    - a vezérlőpanelt (játék mód, nehézség, mentés/betöltés gombok).

- **`BoardView`**
  - A tábla grafikus megjelenítése (rács, korongok).
  - Egérkattintásra jelzi a `GameManager` felé, hogy melyik oszlopba szeretne lépni a felhasználó.
  - A `GameState` alapján újrarajzolja a táblát.

---

### Controller / Service réteg

- **`GameManager`**
  - A játékmenet központi irányítója.
  - Felel:
    - új játék indítása,
    - lépések kezelése (emberi és AI),
    - győzelem/döntetlen detektálása,
    - kommunikáció a `BoardView` és az AI között,
    - mentés/betöltés kezdeményezése.
  - Birtokol:
    - egy `GameState`-et,
    - egy `GameRepository`-t,
    - egy vagy két `MinimaxAI` példányt (pl. AI vs. AI módhoz).

- **`PersistenceManager`**
  - JSON alapú mentés/betöltés.
  - Felel:
    - aktuális `GameState` exportálása JSON-be,
    - JSON-ből `GameState` visszaállítása.
  - Offline mentéshez, gyors visszatöltéshez, illetve beadandó szempontból jól demonstrálható.

---

### Repository + Database réteg

- **`DatabaseManager`**
  - Tiszta **JDBC** kapcsolatkezelés MySQL felé.
  - Felel:
    - kapcsolat létrehozása (`Connection`),
    - adatbázis és táblák létrehozása, ha nem léteznek,
    - tranzakciók kezelése.

- **`GameRepository`**
  - A magasabb szintű adatbázis műveletek:
    - felhasználók mentése / lekérdezése,
    - játék metaadatok mentése (`games`),
  - A `DatabaseManager`-t használja a konkrét SQL végrehajtásra.

---

### AI réteg – Minimax + Alpha–Beta

- **`MinimaxAI`**
  - A gépi ellenfél fő osztálya.
  - Bemenet:
    - aktuális `GameState` / `Board`,
    - maximális mélység (nehézség),
    - saját játékos (`Player`).
  - Kimenet:
    - a választott `Move` (oszlop index).
  - Működés:
    - Minimax fa építése a lehetséges lépésekből,
    - **Alpha–Beta vágás** a felesleges ágak levágására,
    - a levelek és köztes állapotok értékelése a `HeuristicEvaluator` segítségével.

- **`HeuristicEvaluator`**
  - Heurisztikus értékelő függvény Connect4-hez.
    - A táblát **4 hosszú ablakokkal** pásztázza:
      - horizontálisan,
      - vertikálisan,
      - két irányú átlóban.
    - Minden 4-es ablakra pontszámot ad:
      - 4 saját korong → **győzelem** (nagyon magas pozitív érték),
      - 3 saját + 1 üres → pl. **+100 pont**,
      - 2 saját + 2 üres → pl. **+10 pont**,
      - 3 ellenfél + 1 üres → **nagy negatív pont**, hogy az AI blokkoljon.
    - **Középső oszlop preferálása**:
      - a középső oszlopban lévő saját korongok extra pontot kapnak,
      - ez több potenciális győzelmi vonalat nyit meg.

