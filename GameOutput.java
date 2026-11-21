/**
 * Anything that can show game messages (console, GUI, ...).
 * Game only talks to this interface, so the user interface can be replaced
 * without touching the game logic (Exercise 6.19: model-view separation).
 */
public interface GameOutput
{
    void show(MessageType type, String text);
}
