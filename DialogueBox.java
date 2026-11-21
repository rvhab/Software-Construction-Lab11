import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

/**
 * A visual-novel style speech box at the bottom of the scene: Willow's round
 * portrait on the left and what just happened on the right. It only shows the
 * messages of the LAST command; the full history lives in the diary.
 */
public class DialogueBox extends GothicPanel
{
    private final JTextPane text = new JTextPane();
    private final StyledDocument doc = text.getStyledDocument();
    private final Portrait portrait = new Portrait();

    public DialogueBox()
    {
        super(new BorderLayout(14, 0), new Color(22, 9, 14, 232), 16);
        setBorder(new EmptyBorder(12, 14, 12, 16));
        MessageStyles.install(doc, 14);
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setBorder(new EmptyBorder(0, 2, 0, 6));
        JScrollPane scroll = new JScrollPane(text);
        Theme.styleScroll(scroll);
        add(portrait, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);
    }

    /** Forget the messages of the previous command. */
    public void beginTurn()
    {
        text.setText("");
    }

    /** Show one message; the first line of a ROOM message is the headline. */
    public void add(MessageType type, String message)
    {
        String[] lines = message.split("\n");
        for(int i = 0; i < lines.length; i++) {
            append(lines[i], MessageStyles.nameFor(type, i == 0));
        }
    }

    /** Willow's face reacts to what is said. */
    public void react(Avatar.Mood mood)
    {
        portrait.react(mood);
    }

    private void append(String line, String styleName)
    {
        try {
            int start = doc.getLength();
            Style style = doc.getStyle(styleName);
            doc.insertString(start, line + "\n", style);
            doc.setParagraphAttributes(start, line.length() + 1, style, false);
            text.setCaretPosition(0);
        }
        catch(BadLocationException ex) {
            // cannot happen: we always insert at the end
        }
    }

    /** A round picture of Willow with her name underneath. */
    private static class Portrait extends JComponent
    {
        private Avatar.Mood mood = Avatar.Mood.NORMAL;
        private long moodUntil;
        private double time;

        Portrait()
        {
            setOpaque(false);
            setPreferredSize(new Dimension(92, 0));
            new javax.swing.Timer(40, e -> {
                time += 0.04;
                if(moodUntil != 0 && System.currentTimeMillis() > moodUntil) {
                    mood = Avatar.Mood.NORMAL;
                    moodUntil = 0;
                }
                if(isShowing()) {
                    repaint();
                }
            }).start();
        }

        void react(Avatar.Mood m)
        {
            mood = m;
            moodUntil = System.currentTimeMillis() + 1800;
        }

        protected void paintComponent(Graphics g0)
        {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.quality(g);
            int d = 76, x = (getWidth() - d) / 2, y = 2;
            g.setPaint(new GradientPaint(0, y, new Color(0x5A1A26), 0, y + d, new Color(0x1E070C)));
            g.fill(new Ellipse2D.Double(x, y, d, d));
            Shape old = g.getClip();
            g.clip(new Ellipse2D.Double(x, y, d, d));
            Theme.drawSparkle(g, x + 14, y + 16, 5, Theme.alpha(Theme.GOLD, 200));
            Avatar.drawHero(g, x + d / 2.0, y + d + 12, 1.05, 0, 0, 0, time, mood, 0, 1);
            g.setClip(old);
            g.setColor(Theme.GOLD);
            g.setStroke(new BasicStroke(2.2f));
            g.draw(new Ellipse2D.Double(x, y, d, d));
            g.setFont(Theme.ui(Font.BOLD, 12));
            String name = "WILLOW";
            g.setColor(Theme.ROSE);
            g.drawString(name, (getWidth() - g.getFontMetrics().stringWidth(name)) / 2, y + d + 18);
            g.dispose();
        }
    }
}
