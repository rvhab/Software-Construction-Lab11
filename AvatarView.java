import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * A small animated picture of Willow the witch. It can optionally show a speech bubble
 * next to the hero, which is how the game "talks" in the header.
 */
public class AvatarView extends JComponent
{
    private final double scale;
    private final boolean bubble;
    private Avatar.Mood baseMood = Avatar.Mood.NORMAL;
    private Avatar.Mood mood = Avatar.Mood.NORMAL;
    private String text = "";
    private long textUntil = 0;
    private long moodUntil = 0;
    private double time = 0;
    private double hop = 0;

    public AvatarView(double scale, boolean bubble)
    {
        this.scale = scale;
        this.bubble = bubble;
        setOpaque(false);
        int h = (int) (scale * 100);
        setPreferredSize(new Dimension(bubble ? 430 : (int) (scale * 70), h));
        new javax.swing.Timer(33, e -> {
            time += 0.033;
            long now = System.currentTimeMillis();
            if(moodUntil != 0 && now > moodUntil) {
                mood = baseMood;
                moodUntil = 0;
            }
            if(isShowing()) {
                repaint();
            }
        }).start();
    }

    /** The mood the hero returns to after a short reaction. */
    public void setBaseMood(Avatar.Mood m)
    {
        baseMood = m;
        mood = m;
        moodUntil = 0;
    }

    /** Make the hero say something and react for a moment. */
    public void say(String message, Avatar.Mood reaction)
    {
        text = message;
        textUntil = System.currentTimeMillis() + 5200;
        mood = reaction;
        moodUntil = System.currentTimeMillis() + 1400;
    }

    protected void paintComponent(Graphics g0)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int h = getHeight();
        double cx = bubble ? 44 : getWidth() / 2.0;
        Avatar.drawHero(g, cx, h - 6, scale, 0, 0, 0, time, mood, 0, 1);
        if(bubble) {
            long left = textUntil - System.currentTimeMillis();
            if(left > 0 && !text.isEmpty()) {
                float alpha = left < 600 ? left / 600f : 1f;
                drawBubble(g, alpha);
            }
        }
        g.dispose();
    }

    private void drawBubble(Graphics2D g, float alpha)
    {
        g.setFont(Theme.ui(Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int maxW = getWidth() - 86 - 44;
        String shown = text;
        if(fm.stringWidth(shown) > maxW) {
            while(shown.length() > 1 && fm.stringWidth(shown + "...") > maxW) {
                shown = shown.substring(0, shown.length() - 1);
            }
            shown = shown.trim() + "...";
        }
        int bw = fm.stringWidth(shown) + 30, bh = 34;
        int bx = 86, by = getHeight() / 2 - bh / 2 - 4;
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRoundRect(bx + 1, by + 3, bw, bh, 26, 26);
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(bx + 2, by + bh / 2 - 6);
        tail.lineTo(bx - 9, by + bh / 2 + 2);
        tail.lineTo(bx + 2, by + bh / 2 + 6);
        tail.closePath();
        g.setColor(Theme.PANEL_LIGHT);
        g.fillRoundRect(bx, by, bw, bh, 26, 26);
        g.fill(tail);
        g.setColor(Theme.alpha(Theme.ROSE, 190));
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(bx, by, bw, bh, 26, 26);
        g.setColor(Theme.PANEL_LIGHT);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(bx + 1, by + bh / 2 - 4, bx + 1, by + bh / 2 + 4);
        g.setColor(Theme.TEXT);
        g.drawString(shown, bx + 15, by + (bh - fm.getHeight()) / 2 + fm.getAscent());
        Theme.drawHeart(g, bx + bw - 9, by + 7, 7, Theme.alpha(Theme.ROSE, 220));
    }
}
