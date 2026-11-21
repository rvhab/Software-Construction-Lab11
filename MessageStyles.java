import java.awt.Color;
import java.awt.Font;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * The text styles used to colour game messages. Both the dialogue box and the
 * diary use them, so the colours of a message type are defined in ONE place.
 */
public final class MessageStyles
{
    private MessageStyles() { }

    /** Add all named styles to a document. */
    public static void install(StyledDocument doc, int baseSize)
    {
        style(doc, "story", Theme.story(Font.PLAIN, baseSize + 2), new Color(0xD6C8FF), 6, 6);
        style(doc, "roomTitle", Theme.story(Font.BOLD, baseSize + 4), Theme.GOLD, 10, 2);
        style(doc, "roomInfo", Theme.ui(Font.PLAIN, baseSize), Theme.TEAL, 0, 1);
        style(doc, "info", Theme.ui(Font.PLAIN, baseSize), Theme.TEXT, 4, 4);
        style(doc, "error", Theme.ui(Font.BOLD, baseSize), Theme.RED, 4, 4);
        style(doc, "success", Theme.ui(Font.BOLD, baseSize), Theme.GREEN, 4, 4);
        style(doc, "speech", Theme.story(Font.PLAIN, baseSize + 1), Theme.ROSE, 4, 4);
        style(doc, "echo", Theme.ui(Font.BOLD, baseSize), Theme.PURPLE, 10, 2);
        style(doc, "system", Theme.ui(Font.PLAIN, baseSize - 2), Theme.MUTED, 2, 2);
    }

    /** @return the style name that belongs to a kind of message. */
    public static String nameFor(MessageType type, boolean firstLine)
    {
        switch(type) {
            case ROOM:    return firstLine ? "roomTitle" : "roomInfo";
            case STORY:   return "story";
            case INFO:    return "info";
            case ERROR:   return "error";
            case SUCCESS: return "success";
            case SPEECH:  return "speech";
            case ECHO:    return "echo";
            default:      return "system";
        }
    }

    private static void style(StyledDocument doc, String name, Font f, Color c, int above, int below)
    {
        Style s = doc.addStyle(name, null);
        StyleConstants.setFontFamily(s, f.getFamily());
        StyleConstants.setFontSize(s, f.getSize());
        StyleConstants.setBold(s, f.isBold());
        StyleConstants.setItalic(s, false);
        StyleConstants.setForeground(s, c);
        StyleConstants.setSpaceAbove(s, above);
        StyleConstants.setSpaceBelow(s, below);
        StyleConstants.setLineSpacing(s, 0.12f);
    }
}
