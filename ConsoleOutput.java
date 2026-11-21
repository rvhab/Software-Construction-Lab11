/** Shows game messages in the text terminal. */
public class ConsoleOutput implements GameOutput
{
    public void show(MessageType type, String text)
    {
        System.out.println(text);
    }
}
