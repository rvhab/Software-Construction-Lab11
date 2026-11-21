import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** A dimmed full-window layer with a centred card (start screen / end screen). */
public class Overlay extends JPanel
{
    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private final JLabel body = new JLabel("", SwingConstants.CENTER);
    private final AvatarView hero = new AvatarView(2.0, false);
    private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));

    public Overlay()
    {
        setOpaque(false);
        setLayout(new GridBagLayout());
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 16), Theme.PANEL, 28);
        card.setBorder(new EmptyBorder(18, 46, 28, 46));
        title.setFont(Theme.ui(Font.BOLD, 32));
        body.setFont(Theme.ui(Font.PLAIN, 14));
        body.setForeground(Theme.TEXT);
        buttons.setOpaque(false);
        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.add(hero, BorderLayout.CENTER);
        head.add(title, BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        add(card);
        addMouseListener(new MouseAdapter() { });               // swallow clicks behind the card
        addMouseMotionListener(new MouseMotionAdapter() { });
    }

    /** How the hero on the card feels (wave on start, happy on a win, sad on a loss). */
    public void setMood(Avatar.Mood mood)
    {
        hero.setBaseMood(mood);
    }

    public void present(String titleText, Color titleColor, String text,
                        String primaryText, Runnable primary, String secondaryText, Runnable secondary)
    {
        title.setText(titleText);
        title.setForeground(titleColor);
        body.setText("<html><div style='text-align:center; width:400px'>" + text + "</div></html>");
        buttons.removeAll();
        FancyButton b1 = new FancyButton(primaryText, FancyButton.Kind.PRIMARY);
        b1.setPreferredSize(new Dimension(180, 42));
        b1.addActionListener(e -> primary.run());
        buttons.add(b1);
        if(secondaryText != null) {
            FancyButton b2 = new FancyButton(secondaryText, FancyButton.Kind.SECONDARY);
            b2.setPreferredSize(new Dimension(110, 42));
            b2.addActionListener(e -> secondary.run());
            buttons.add(b2);
        }
        setVisible(true);
        revalidate();
        repaint();
    }

    protected void paintComponent(Graphics g)
    {
        g.setColor(new Color(6, 3, 14, 205));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
