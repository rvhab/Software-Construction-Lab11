import java.awt.*;
import java.awt.geom.*;

/**
 * Draws a small picture for every item of the game (lantern, spell book, key...).
 * Pure drawing: it only needs the item's name, so the same icons are used on
 * the floor of the scene and in the pockets.
 */
public final class ItemArt
{
    private ItemArt() { }

    /**
     * Draw the icon of an item.
     *
     * @param name the item name, e.g. "lantern"
     * @param cx   horizontal centre in pixels
     * @param cy   vertical centre in pixels
     * @param size icon width in pixels
     * @param t    time in seconds (glow and bobbing)
     * @param glow true to make the icon shine (e.g. a charged orb)
     */
    public static void draw(Graphics2D g0, String name, double cx, double cy, double size, double t, boolean glow)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        g.translate(cx, cy);
        double k = size / 24.0;
        g.scale(k, k);

        if(glow) {
            halo(g, 0, 0, 20, Theme.GOLD, 0.35 + 0.15 * Math.sin(t * 4));
        }
        switch(name) {
            case "lantern": lantern(g, t); break;
            case "grimoire": grimoire(g, t); break;
            case "cauldron": cauldron(g, t); break;
            case "key": key(g); break;
            case "orb": orb(g, t, glow); break;
            case "cookie": cookie(g, t); break;
            case "scroll": scroll(g); break;
            default: sack(g); break;
        }
        g.dispose();
    }

    private static void halo(Graphics2D g, double x, double y, double r, Color c, double strength)
    {
        float[] f = { 0f, 1f };
        Color[] cols = { Theme.alpha(c, (int) (255 * strength)), Theme.alpha(c, 0) };
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) r, f, cols));
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }

    private static void lantern(Graphics2D g, double t)
    {
        halo(g, 0, 1, 16, new Color(0xFFE08A), 0.35);
        g.setColor(Theme.GOLD_DARK);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-4.5, -13, 9, 9, 0, 180, Arc2D.OPEN));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0x3A2A5C));
        g.fill(new RoundRectangle2D.Double(-6, -5, 12, 3, 2, 2));
        g.setPaint(new GradientPaint(0, -3, new Color(0xFFF1B8), 0, 9, new Color(0xFFB84D)));
        g.fill(new RoundRectangle2D.Double(-5.5, -3, 11, 12, 4, 4));
        g.setColor(new Color(0x3A2A5C));
        g.fill(new RoundRectangle2D.Double(-6.5, 8, 13, 3.4, 2, 2));
        double glow = 0.6 + 0.4 * Math.sin(t * 5);                       // the firefly
        g.setColor(Theme.alpha(Color.WHITE, (int) (200 * glow)));
        g.fill(new Ellipse2D.Double(-1.6 + Math.sin(t * 2) * 1.5, 1 + Math.cos(t * 3) * 1.2, 3.2, 3.2));
    }

    private static void grimoire(Graphics2D g, double t)
    {
        g.setColor(new Color(0x2A1760));
        g.fill(new RoundRectangle2D.Double(-8.5, -10, 17, 21, 3, 3));
        g.setPaint(new GradientPaint(0, -10, new Color(0x9568F5), 0, 10, new Color(0x5B34C4)));
        g.fill(new RoundRectangle2D.Double(-8, -10.5, 16, 20.5, 3, 3));
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(6.5, -8.5, 1.8, 17));              // page edge
        g.setColor(Theme.GOLD);
        g.fill(new Rectangle2D.Double(-8, -6, 16, 1.4));
        g.fill(new Rectangle2D.Double(-8, 5, 16, 1.4));
        Theme.drawStar(g, -0.5, -0.5, 3.8 + Math.sin(t * 3) * 0.3, Theme.GOLD);
    }

    private static void cauldron(Graphics2D g, double t)
    {
        g.setColor(new Color(0x1E1830));
        g.fill(new Rectangle2D.Double(-7, 7, 3, 4));
        g.fill(new Rectangle2D.Double(4, 7, 3, 4));
        g.setPaint(new GradientPaint(0, -4, new Color(0x4A4266), 0, 10, new Color(0x1E1830)));
        g.fill(new Ellipse2D.Double(-10, -5, 20, 16));
        g.setColor(new Color(0x2C2546));
        g.fill(new Ellipse2D.Double(-10.5, -7.5, 21, 7));
        g.setColor(Theme.MINT);
        g.fill(new Ellipse2D.Double(-8.5, -6.7, 17, 5));
        for(int i = 0; i < 3; i++) {
            double ph = (t * 1.2 + i * 0.33) % 1.0;
            g.setColor(Theme.alpha(Theme.MINT, (int) (220 * (1 - ph))));
            g.fill(new Ellipse2D.Double(-4 + i * 4.5, -8 - ph * 8, 3, 3));
        }
    }

    private static void key(Graphics2D g)
    {
        g.rotate(-0.6);
        g.setColor(Theme.GOLD);
        Area head = new Area(new Ellipse2D.Double(-6, -12, 12, 12));
        head.subtract(new Area(new Ellipse2D.Double(-2.6, -8.6, 5.2, 5.2)));
        g.fill(head);
        g.fill(new RoundRectangle2D.Double(-1.5, -1, 3, 14, 1.5, 1.5));
        g.fill(new Rectangle2D.Double(1.5, 7, 4.5, 2.2));
        g.fill(new Rectangle2D.Double(1.5, 10.5, 3.2, 2.2));
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Double(-4.5, -10.5, 3, 2));
    }

    private static void orb(Graphics2D g, double t, boolean charged)
    {
        halo(g, 0, -1, 16, Theme.ROSE, charged ? 0.6 : 0.3);
        g.setColor(Theme.GOLD_DARK);
        g.fill(new RoundRectangle2D.Double(-7, 7, 14, 4, 3, 3));
        float[] f = { 0f, 0.6f, 1f };
        Color[] c = { new Color(0xFFD9F5), new Color(0xB07BFF), new Color(0x4A2AA0) };
        g.setPaint(new RadialGradientPaint(new Point2D.Double(-3, -6), 15f, f, c));
        g.fill(new Ellipse2D.Double(-9, -10, 18, 18));
        g.setColor(new Color(255, 255, 255, 190));
        g.fill(new Ellipse2D.Double(-5.8, -7.8, 4.6, 3));
        Theme.drawSparkle(g, 2 + Math.sin(t * 2) * 2, -1 + Math.cos(t * 2.4) * 2, 2.5, Color.WHITE);
    }

    private static void cookie(Graphics2D g, double t)
    {
        halo(g, 0, 0, 15, Theme.GOLD, 0.3 + 0.1 * Math.sin(t * 4));
        g.setPaint(new GradientPaint(0, -9, new Color(0xF4C27A), 0, 9, new Color(0xC98A45)));
        g.fill(new Ellipse2D.Double(-9, -9, 18, 18));
        g.setColor(new Color(0x7A4A28));
        g.fill(new Ellipse2D.Double(-5, -5, 3, 3));
        g.fill(new Ellipse2D.Double(2, -2, 3, 3));
        g.fill(new Ellipse2D.Double(-3, 3, 2.6, 2.6));
        Theme.drawStar(g, 3.5, -5.5, 3.2, Theme.GOLD);
    }

    private static void scroll(Graphics2D g)
    {
        g.setColor(new Color(0xE8D6B0));
        g.fill(new RoundRectangle2D.Double(-6, -9, 12, 18, 3, 3));
        g.setColor(new Color(0xFFF1D4));
        g.fill(new RoundRectangle2D.Double(-8.5, -10, 5, 20, 5, 5));
        g.fill(new RoundRectangle2D.Double(3.5, -10, 5, 20, 5, 5));
        g.setColor(new Color(0xB89A6A));
        g.setStroke(new BasicStroke(0.9f));
        for(int i = 0; i < 4; i++) {
            g.draw(new Line2D.Double(-3, -5 + i * 3.4, 3, -5 + i * 3.4));
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(Theme.ROSE_DARK);
        g.fill(new Rectangle2D.Double(-1, -10, 2, 20));
    }

    private static void sack(Graphics2D g)
    {
        g.setColor(new Color(0x8E63F0));
        g.fill(new Ellipse2D.Double(-8, -5, 16, 16));
        g.setColor(Theme.ROSE);
        g.fill(new RoundRectangle2D.Double(-4, -8, 8, 4, 3, 3));
        Theme.drawStar(g, 0, 3, 3, Theme.GOLD);
    }
}
