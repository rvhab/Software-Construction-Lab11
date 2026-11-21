WORLD OF ZUUL - "Horror Castle"   (Lab 11: Designing Classes - II)

WITCH EDITION: the castle is gone. You play Willow, a cute little witch trapped in her own
crooked cottage by a sleepy spell. The GUI was rebuilt in a new format: one big animated
night-time scene fills the whole window and small floating panels sit on top, like a video game.

  * Scene      - every room has its own drawn backdrop (blood-red fireplace, bubbling cauldron, floating
                 cursed book, blood-moon bell tower, dungeon of glowing skulls, crimson portal, graveyard gate).
                 Willow walks in from the side you came from and vanishes in stars when teleported.
  * Click play - click a glowing exit sign to walk, click an item on the floor to take it,
                 click the cat or the ghost to talk. Buttons and typed commands work too.
  * Panels     - gothic frames (arch-topped map with tombstone rooms, coffin-slot pockets). Top: title, room name, meters. Left: castle map + pockets (click an item for
                 Eat / Give / Drop). Right: diary (full history). Bottom: Willow's speech box + commands.

HOW TO RUN THE GUI (recommended): Compile, then right-click class GameGUI > void main(String[] args).
TEXT VERSION: right-click class Game > void main(String[] args).
Outside BlueJ:  javac *.java   then   java GameGUI   (or java Game)

GOAL: find the rusty key, unlock the gate (south of the great hall) and escape within 60 commands.

COMMANDS: go <dir>, look, back, items, take <item>, drop <item>, eat <item>,
unlock <dir>, talk <person>, give <item>, charge, fire, help, quit.
(The "beamer" of the exercises is Willow's crystal orb: charge memorises a room, fire returns you.)
Any unknown command, missing/extra word, or impossible action prints an error.

WALKTHROUGH: go east, take orb, go west, charge, go north, go down,
take key, fire, unlock south, go south.

CLASS OVERVIEW (high cohesion: one job per class)
  Game, Room, Player, Item, Beamer, GameCharacter, Parser, Command, CommandWord(s)  - game logic, no Swing
  GameOutput, MessageType, ConsoleOutput                                             - how messages leave the game
  GameGUI      - window + controller      ScenePanel - animated scene and clicks   RoomArt - room backdrops
  ItemArt      - item icons               Avatar     - witch, cat, ghost drawings  DialogueBox - speech box
  Hotbar       - pockets                  MapPanel   - castle map                 MessageStyles - text colours
  GothicPanel (panel frames), Overlay, FancyButton, BarMeter, RoundedPanel, Theme - small reusable GUI parts

Written answers to every exercise of Chapter 6: see ANSWERS.md
