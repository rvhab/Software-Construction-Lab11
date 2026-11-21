# Lab 11 – Designing Classes II: Answers to Chapter 6 Exercises

Project: **World of Zuul – "Horror Castle"** (BlueJ project, open `package.bluej`).
Code answers are implemented in the project; written answers are below.

---
## 6.1 – 6.2  Exploring the original zuul-bad game
**6.1**
- *What does the application do?* A simple text-based adventure: the player walks between rooms of a university by typing commands.
- *Commands accepted (original):* `go`, `quit`, `help`.
- *What each does:* `go <direction>` moves through an exit (north/east/south/west); `help` prints a help text and the command words; `quit` ends the game.
- *Number of rooms:* 5 – outside, theater, pub, lab, office.
- *Map:*
```
          pub — outside — theater
                  |
                 lab — office
```
**6.2 Purpose of each class**
| Class | Purpose |
|---|---|
| CommandWords | Defines all valid command words. |
| Parser | Reads a line from the terminal and turns it into a `Command`. |
| Command | Holds the first and second word typed; can tell if the command is unknown / has a second word. |
| Room | A location with exits to other rooms. |
| Game | Main class: sets up the game, runs the command loop and implements each command. |

## 6.3 – 6.4  My own game and its map
**6.3 Scenario:** Willow, a little witch, wakes up trapped in a cursed castle. She must find a rusty iron key (hidden in the dungeon), unlock the gate in the great hall and escape within 60 commands. There is a one-way trapdoor, a transporter room, a magic cookie, a crystal orb (the beamer), a cat who trades a hint for a lantern, and a wandering ghost.

**6.4 Map** (implemented in `Game.createRooms`):
```
                 [bell tower]
                    | up/down
 [cursed kitchen] — [ great hall ] — south (LOCKED) → [graveyard gate = EXIT]
    west/east    | north/south
              [forbidden library]
                    | down  (TRAPDOOR – one way!)
               [dungeon] — east/west — [portal room: teleports you randomly]
```

## 6.5  printLocationInfo
Implemented as a separate cohesive method. In the final design it moved further: `Room.getLongDescription()` produces the text and `Game.showLocation()` prints it (see 6.11).

## 6.6 – 6.8  Encapsulation, accessor, HashMap
- Room fields are all `private`; `Room.getExit(String direction)` is the accessor (6.6). Game uses `currentRoom.getExit(direction)` instead of the long if-chain.
- `Room.getExitString()` builds "Exits: north west" inside Room (6.7).
- Exits are stored in `HashMap<String, Room> exits`; `setExits(n,e,s,w)` was replaced by `setExit(String direction, Room neighbor)` (6.8). Any direction (up, down, northwest…) works with no further change.

## 6.9  keySet
`HashMap.keySet()` returns a `Set` view containing all keys of the map (here: all direction names that have an exit). It has no duplicates and is not ordered.

## 6.10  How getExitString works
```java
String returnString = "Exits:";
Set<String> keys = exits.keySet();
for(String exit : keys) { returnString += " " + exit; }
return returnString;
```
It starts with the text "Exits:", gets the set of keys (directions) of the `exits` map, loops over every key with a for-each loop and appends a space plus the key to the string, then returns the finished string, e.g. `"Exits: north west"`. Because it only uses the map, it works for any number of exits. (My version also appends "(locked)" for locked exits.)

## 6.11  getLongDescription
`Room.getLongDescription()` returns "You are …", the exits, and any items and people. Game only calls `System.out.println(currentRoom.getLongDescription())`. Future additions (items, characters) needed a change in Room only.

## 6.12  Object diagram just after starting
```
Game ──► Parser ──► CommandWords
  │
  ├──► Player(name "Adventurer", maxWeight 2500, items {}, history [ ]) ──currentRoom──► hall
  ├──► rooms: hall, kitchen, library, tower, cellar, portal, garden
  │         each Room: description, exits(HashMap), lockedExits, items(HashMap), characters(HashMap)
  │         hall: items {lantern}   kitchen: {orb, cookie}   library: {grimoire, cauldron, ghost}
  │         cellar: {key}   tower: {cat}   hearth exit "south" is locked
  └──► movingCharacters [ghost]
```
## 6.13  Change after a `go` command
`player.currentRoom` now refers to the new room, and the previous room is pushed onto the player's `history` stack. No other object changes (moving characters may also change room).

## 6.14  look command
Implemented: `CommandWord.LOOK("look")`, `Game.look()` prints `getLongDescription()`, registered in `registerActions()`.

## 6.15  Another command
Implemented `eat <item>` (eats a carried edible item). `drop`, `take`, `items`, `back`, `talk`, `give`, `unlock`, `charge`, `fire` were also added.

## 6.16  Printing command words via Parser
`help` calls `parser.getCommands()`, which delegates to `CommandWords.getCommandList()`. Game never references CommandWords (loose coupling).

## 6.17  Do I still need to change Game for a new command?
Help text: **no** – it is generated from the enum/map. But the new command still needs an implementation method in Game (and a one-line registration in `registerActions`), because Game knows how to carry out commands. No change is needed to the help text, `CommandWords`, or `Parser`.

## 6.18  getCommandList returns a String
Done: `CommandWords.getCommandList()` returns a string instead of printing; Game decides how to show it – good for a future GUI.

## 6.19  Model-View-Controller
MVC separates an application into **Model** (data and rules), **View** (presentation) and **Controller** (handles input and updates model/view). Here: Model = Room, Item, Player, GameCharacter, Beamer; View = the code that prints text (currently inside Game); Controller = Parser/Command/Game's command dispatch. **Implemented in this project:** Game no longer prints directly; it sends messages to a `GameOutput` interface. `ConsoleOutput` is the text view and `GameGUI` (Swing) is the graphical view, so the model (Room, Player, Item, …) is identical for both. This is why `showAll` was changed to return a String.

## 6.20 – 6.22  Items
- `Item` class (name, description, weight, canBePickedUp). Rooms hold items in a `HashMap<String, Item>` (any number of items – 6.22) with `addItem`, `getItem`, `removeItem`; all items are listed when entering a room (`getItemString`).
**6.21** Which class produces the item text? `Item.getLongDescription()` produces the text for one item (the Item knows its own data); `Room.getItemString()` assembles the list for the room; `Game` only prints. This keeps responsibility with the class that owns the data.

## 6.23 – 6.26  back command
`Player` keeps `Stack<Room> history`. `moveTo` pushes the current room; `goBack` pops it.
- **6.24** Works; `back north` prints "'back' does not take a second word."
- **6.25** Typing `back` twice goes back two rooms (because of the stack). When the stack is empty it prints "You cannot go back…". With a single `previousRoom` field the second `back` would just bounce between two rooms – not sensible; the stack fixes this.
- **6.26** Challenge done with a Stack.

## 6.27  Baseline tests for the game
1. Start: welcome text, correct room, exits shown. 2. `help` lists all commands. 3. `go` in each valid direction; `go` with invalid direction ("There is no door!"); `go` with no word ("Go what?"). 4. Unknown word, empty line, three-word line → error. 5. `look`. 6. `quit` ends. 7. Locked door message. 8. take/drop/items weight limit. 9. back. Record the expected output and repeat after refactoring.

## 6.28 – 6.33  Player, take/drop, items, weight, cookie
- **6.28** `Player` class (name, currentRoom, items, maxWeight, history); `currentRoom` moved from Game to Player.
- **6.29 / 6.30** `take <item>` and `drop <item>`; any number of items stored in a HashMap.
- **6.31** Weight limit: `Player.takeItem` refuses if total + item weight > `maxWeight`. (Initial limit 2500 g.) *Design answer (p. 224):* I used a `canBePickedUp` boolean (the statue) rather than just "very heavy", because "cannot be picked up" is a separate idea from "too heavy for this player" – a stronger player or a cookie could change the second but never the first.
- **6.32** `items` prints carried items and total weight.
- **6.33** Magic cookie in the kitchen; `eat cookie` raises max weight by 1500 g.

## 6.34 – 6.40  Enumerated types
- **6.34** `CommandWord` enum is used by Command, CommandWords, Parser and Game (switch replaced by an action map).
- **6.35 / 6.39** `look` added – only `CommandWord` (new value + text) plus the Game method/registration was needed.
- **6.36** To translate `go`/`quit` I only edit the text strings in `CommandWord` (e.g. `GO("gehe")`). Significance: the command text is stored in exactly one place, so game logic is independent of the language.
- **6.37 / 6.40** Changing the HELP word is automatically reflected in the welcome message, because `printWelcome` uses `CommandWord.HELP` (its `toString`) instead of hard-coded text.
- **6.38** `Position` enum with TOP, MIDDLE, BOTTOM (file `Position.java`).

## 6.41 – 6.48  Extensions
| Ex. | Implementation |
|---|---|
| 6.41 Time limit | `MAX_COMMANDS = 60` in Game; warning at 10 left; "YOU LOSE" at 0. |
| 6.42 Trapdoor | library `down` → cellar, no exit back. |
| 6.43 Beamer | `Beamer extends Item`; commands `charge`, `fire`. |
| 6.44 Locked doors | `Room.lockExit/unlockExit/isLocked`; `unlock <dir>` needs the `key`. |
| 6.45 Transporter room | `portal.setTransporter(true)`; Game teleports the player to a random room (list `teleportTargets`). *Design note:* the room only has a flag; the Game decides where to send the player, so Room does not need to know about other rooms. |
| 6.46 Challenge: command dispatch | `HashMap<CommandWord, Action>` with method references replaces the if/switch chain. A new command = enum value + method + one `actions.put` line. |
| 6.47 Characters | `GameCharacter` (cat): `talk cat`; `give lantern` → cat gives a scroll with a hint. |
| 6.48 Moving characters | Ghost has `moving = true` and wanders to a random neighbour after every command; Game reports when it enters/leaves the player's room. |

## 6.49 – 6.53  Static methods, main
- **6.49** `Math.max` – signature: `public static int max(int a, int b)` (also overloaded for long, float, double).
- **6.50** Math methods are static because they use no object state – they only compute from their parameters. Creating a `Math` object just to call them would be pointless; written as instance methods they'd work but be inconvenient.
- **6.51** `TimingTest.java` (uses `System.nanoTime`/`currentTimeMillis`). Counting to 100 takes only microseconds, so `currentTimeMillis` usually shows 0 ms – use `nanoTime` for such short intervals.
- **6.52** `public static void main(String[] args) { new Game().play(); }` is in `Game`.
- **6.53** Without BlueJ: `javac *.java` then `java Game`.

## 6.54  TechSupport without BlueJ
(The TechSupport project from Chapter 5 is not part of this lab project.) Steps: add to the class that starts the program (e.g. `SupportSystem`) `public static void main(String[] args){ new SupportSystem().start(); }`, open a terminal in the project folder, run `javac *.java`, then `java SupportSystem`.

## 6.55  Static vs instance method calls
- Static → static: **yes**. Instance → static: **yes**. Instance → instance: **yes**.
- Static → instance method (or instance field) of the same class directly: **no** – compile error "non-static method cannot be referenced from a static context", because there is no `this` object. It is possible only through an object: `new StaticDemo().instanceA()`.
- See `StaticDemo.java`.

## 6.56  Counting instances
Yes. Use a **static (class) variable** incremented in the constructor, and a static method returning it:
```java
private static int instances = 0;
public InstanceCounter() { instances++; }
public static int numberOfInstances() { return instances; }
```
See `InstanceCounter.java`. (A normal instance field would not work – every object would have its own copy starting at 0.)

---
## Lab Task checklist
- Own version of world-of-zuul – yes.
- Invalid commands generate an error – unknown words, empty line, >2 words, missing second word, extra second word, unknown direction/item/person, locked door, etc.
- High cohesion / loose coupling – private fields, accessors, responsibility-driven methods (`Room.getLongDescription`, `Player.takeItem`, …), Game never touches CommandWords or Room internals, no code duplication.

---
## Bonus: Graphical interface (GameGUI)
Run `GameGUI.main`. Features: a full-window animated scene of the current room with Willow the witch (she walks in, hops, cheers or cries), clickable exit signs / items / friends, a cottage map with fog of war, pockets grid with Eat/Give/Drop menu, a speech box for the latest messages, a diary with the full history, animated meters for commands left and carried weight, a command line with history (Up/Down), and start / win / lose screens. All game rules stay in the model classes; the GUI classes (`GameGUI`, `ScenePanel`, `RoomArt`, `ItemArt`, `Avatar`, `DialogueBox`, `Hotbar`, `MapPanel`, `Overlay`, `FancyButton`, `RoundedPanel`, `BarMeter`, `MessageStyles`, `Theme`) only display state and send commands.
