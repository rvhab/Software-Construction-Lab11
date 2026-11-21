import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Draws the illustrated background of every room: wall, floor and the props
 * that make the room recognisable (fireplace, cauldron, bookshelves...).
 * Pure drawing - it knows only the room NAME, never the game rules.
 */
public final class RoomArt
{
    private RoomArt() { }

    /**
     * @param room  the room name, e.g. "Great Hall"
     * @param w,h   size of the whole scene in pixels
     * @param cx    horizontal centre of the visible area
     * @param fy    y coordinate where the wall meets the floor
     * @param u     size unit (props scale with the window)
     * @param t     time in seconds, for animation
     */
    public static void draw(Graphics2D g, String room, int w, int h, int cx, int fy, double u, double t)
    {
        boolean outdoors = room.equals("Graveyard Gate");
        Color[] wall = wallColors(room);
        if(outdoors) {
            night(g, w, fy, t);
        }
        else {
            g.setPaint(new GradientPaint(0, 0, wall[0], 0, fy, wall[1]));
            g.fillRect(0, 0, w, fy);
            wallTexture(g, room, w, fy, u);
        }
        floor(g, room, w, h, cx, fy, u, outdoors);

        switch(room) {
            case "Great Hall":     hearth(g, cx, fy, u, t); break;
            case "Cursed Kitchen":  kitchen(g, cx, fy, u, t); break;
            case "Forbidden Library":   library(g, cx, fy, u, t); break;
            case "Bell Tower":      tower(g, cx, fy, u, t); break;
            case "Dungeon": cellar(g, cx, fy, u, t); break;
            case "Cursed Portal":     portal(g, cx, fy, u, t); break;
            default:                garden(g, w, cx, fy, u, t); break;
        }
        cobweb(g, 0, 0, 1, u);
        cobweb(g, w, 0, -1, u);
        for(int i = 0; i < 3; i++) {
            bat(g, (w * (0.2 + 0.3 * i) + t * (40 + i * 15)) % (w + 120) - 60, 120 * u + Math.sin(t * 2 + i) * 40 * u + i * 40 * u, 16 * u, t + i);
        }
    }

    private static void cobweb(Graphics2D g, double x, double y, int side, double u)
    {
        g.setColor(new Color(255, 255, 255, 45));
        g.setStroke(new BasicStroke(1.1f));
        double R = 170 * u;
        for(int k = 0; k <= 5; k++) {
            double a = Math.toRadians(k * 18);
            g.draw(new Line2D.Double(x, y, x + side * Math.cos(a) * R, y + Math.sin(a) * R));
        }
        for(int ring = 1; ring <= 5; ring++) {
            double rr = R * ring / 5.0;
            Path2D.Double p = new Path2D.Double();
            for(int k = 0; k <= 5; k++) {
                double a = Math.toRadians(k * 18);
                double px = x + side * Math.cos(a) * rr, py = y + Math.sin(a) * rr * 0.98 - (k % 1) * 0;
                double sag = (k > 0 && k < 5) ? -rr * 0.05 : 0;
                if(k == 0) { p.moveTo(px, py); } else { p.quadTo((x + px) / 2 + side * sag, (y + py) / 2 + rr * 0.12, px, py); }
            }
            g.draw(p);
        }
        g.setStroke(new BasicStroke(1f));
    }

    private static void bat(Graphics2D g, double x, double y, double s, double t)
    {
        double flap = Math.sin(t * 12) * s * 0.6;
        g.setColor(new Color(8, 4, 8, 200));
        Path2D.Double p = new Path2D.Double();
        p.moveTo(x, y);
        p.quadTo(x - s, y - s * 0.6 - flap, x - 2 * s, y + s * 0.1);
        p.quadTo(x - s, y + s * 0.2, x, y + s * 0.5);
        p.quadTo(x + s, y + s * 0.2, x + 2 * s, y + s * 0.1);
        p.quadTo(x + s, y - s * 0.6 - flap, x, y);
        g.fill(p);
        g.fill(new Ellipse2D.Double(x - s * 0.25, y - s * 0.2, s * 0.5, s * 0.6));
        g.setColor(new Color(0xFF3A3A));
        g.fill(new Ellipse2D.Double(x - s * 0.15, y - s * 0.05, s * 0.1, s * 0.1));
        g.fill(new Ellipse2D.Double(x + s * 0.05, y - s * 0.05, s * 0.1, s * 0.1));
    }

    // ------------------------------------------------------------------ shared parts

    private static Color[] wallColors(String room)
    {
        switch(room) {
            case "Great Hall":       return new Color[] { new Color(0x2E2228), new Color(0x170F14) };
            case "Cursed Kitchen":   return new Color[] { new Color(0x2C2A22), new Color(0x161510) };
            case "Forbidden Library":return new Color[] { new Color(0x2A2036), new Color(0x120D1A) };
            case "Bell Tower":       return new Color[] { new Color(0x2A2A3A), new Color(0x121218) };
            case "Dungeon":          return new Color[] { new Color(0x1E2B2E), new Color(0x0C1214) };
            default:                 return new Color[] { new Color(0x3A1420), new Color(0x1A070E) };
        }
    }

    private static void wallTexture(Graphics2D g, String room, int w, int fy, double u)
    {
        g.setColor(new Color(255, 255, 255, 12));
        boolean bricks = room.equals("Dungeon") || room.equals("Cursed Portal");
        int row = (int) (bricks ? 26 * u : 44 * u);
        int r = 0;
        for(int y = 0; y < fy; y += row, r++) {
            g.drawLine(0, y, w, y);
            if(bricks) {
                int bw = (int) (70 * u);
                for(int x = (r % 2) * bw / 2; x < w; x += bw) {
                    g.drawLine(x, y, x, y + row);
                }
            }
        }
    }

    private static void floor(Graphics2D g, String room, int w, int h, int cx, int fy, double u, boolean outdoors)
    {
        Color a = outdoors ? new Color(0x1B2E22) : room.equals("Great Hall") ? new Color(0x3A2A2E)
                : room.equals("Cursed Kitchen") ? new Color(0x34322A) : room.equals("Forbidden Library") ? new Color(0x2E2438)
                : room.equals("Bell Tower") ? new Color(0x34343F) : room.equals("Dungeon") ? new Color(0x222B2E)
                : new Color(0x3A1620);
        Color b = Theme.blend(a, Color.BLACK, 0.55f);
        g.setPaint(new GradientPaint(0, fy, a, 0, h, b));
        g.fillRect(0, fy, w, h - fy);
        g.setColor(Theme.alpha(Color.BLACK, 70));
        g.fillRect(0, fy, w, (int) (9 * u));                              // shadow under the wall
        if(!outdoors) {
            g.setColor(new Color(255, 255, 255, 14));                     // planks in perspective
            for(int i = -14; i <= 14; i++) {
                g.drawLine((int) (cx + i * 150 * u), h, (int) (cx + i * 46 * u), fy);
            }
            for(int k = 1; k < 7; k++) {
                int y = fy + (int) ((h - fy) * Math.pow(k / 7.0, 1.6));
                g.drawLine(0, y, w, y);
            }
        }
    }

    private static void night(Graphics2D g, int w, int fy, double t)
    {
        g.setPaint(new GradientPaint(0, 0, new Color(0x060306), 0, fy, new Color(0x4A0F1E)));
        g.fillRect(0, 0, w, fy);
        stars(g, 0, 0, w, fy, 70, t, 5);
    }

    /** Twinkling stars at fixed (seeded) positions inside a rectangle. */
    private static void stars(Graphics2D g, double x, double y, double w, double h, int n, double t, long seed)
    {
        Random r = new Random(seed);
        for(int i = 0; i < n; i++) {
            double sx = x + r.nextDouble() * w, sy = y + r.nextDouble() * h;
            double size = 1.2 + r.nextDouble() * 2.6, ph = r.nextDouble() * 6.3;
            double tw = 0.5 + 0.5 * Math.sin(t * 2 + ph);
            Theme.drawSparkle(g, sx, sy, size * (0.7 + 0.5 * tw), Theme.alpha(i % 3 == 0 ? Theme.ROSE : Color.WHITE, (int) (90 + 140 * tw)));
        }
    }

    private static void moon(Graphics2D g, double x, double y, double r)
    {
        float[] f = { 0f, 1f };
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) (r * 2.6), f,
                   new Color[] { new Color(255, 60, 60, 110), new Color(255, 60, 60, 0) }));
        g.fill(new Ellipse2D.Double(x - r * 2.6, y - r * 2.6, r * 5.2, r * 5.2));
        Area c = new Area(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        c.subtract(new Area(new Ellipse2D.Double(x - r * 0.55, y - r * 1.15, r * 2, r * 2)));
        g.setPaint(new GradientPaint((float) x, (float) (y - r), new Color(0xFF9A8A), (float) x, (float) (y + r), new Color(0xC41E2E)));
        g.fill(c);
    }

    /** An arched window that looks out into the night. */
    private static void window(Graphics2D g, double x, double y, double w, double h, double t, long seed)
    {
        Path2D.Double arch = new Path2D.Double();
        arch.moveTo(x, y + h);
        arch.lineTo(x, y + w / 2);
        arch.append(new Arc2D.Double(x, y, w, w, 180, -180, Arc2D.OPEN), true);
        arch.lineTo(x + w, y + h);
        arch.closePath();
        g.setColor(new Color(0x4A4450));
        Stroke s = g.getStroke();
        g.setStroke(new BasicStroke((float) (w * 0.09)));
        g.setPaint(new GradientPaint(0, (float) y, new Color(0x0A0408), 0, (float) (y + h), new Color(0x4A1020)));
        g.fill(arch);
        Shape clip = g.getClip();
        g.clip(arch);
        stars(g, x, y, w, h, 14, t, seed);
        moon(g, x + w * 0.66, y + w * 0.42, w * 0.13);
        g.setClip(clip);
        g.setColor(new Color(0x6A6070));
        g.draw(arch);
        g.setStroke(new BasicStroke((float) (w * 0.05)));
        g.draw(new Line2D.Double(x + w / 2, y, x + w / 2, y + h));
        g.draw(new Line2D.Double(x, y + h * 0.5, x + w, y + h * 0.5));
        g.setStroke(s);
    }

    private static void glow(Graphics2D g, double x, double y, double r, Color c, int alpha)
    {
        float[] f = { 0f, 1f };
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) r, f,
                   new Color[] { Theme.alpha(c, alpha), Theme.alpha(c, 0) }));
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }

    private static void flame(Graphics2D g, double x, double base, double h, double wd, Color outer, Color inner)
    {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(x, base);
        p.curveTo(x - wd, base - h * 0.35, x - wd * 0.3, base - h * 0.7, x, base - h);
        p.curveTo(x + wd * 0.3, base - h * 0.7, x + wd, base - h * 0.35, x, base);
        g.setColor(outer);
        g.fill(p);
        g.setColor(inner);
        g.fill(new Ellipse2D.Double(x - wd * 0.4, base - h * 0.5, wd * 0.8, h * 0.5));
    }

    // ------------------------------------------------------------------ the rooms

    private static void hearth(Graphics2D g, int cx, int fy, double u, double t)
    {
        window(g, cx - 380 * u, fy - 270 * u, 130 * u, 190 * u, t, 11);
        double pulse = 0.8 + 0.2 * Math.sin(t * 7);
        glow(g, cx, fy - 70 * u, 300 * u, new Color(0xFF8A3D), (int) (70 * pulse));
        // rug
        g.setColor(new Color(0x6A1A28));
        g.fill(new Ellipse2D.Double(cx - 230 * u, fy + 120 * u, 460 * u, 110 * u));
        g.setColor(new Color(0x9A2A3E));
        g.fill(new Ellipse2D.Double(cx - 190 * u, fy + 132 * u, 380 * u, 86 * u));
        g.setColor(new Color(0x6A1A28));
        g.fill(new Ellipse2D.Double(cx - 120 * u, fy + 146 * u, 240 * u, 58 * u));
        // chimney breast and opening
        g.setPaint(new GradientPaint(0, (float) (fy - 230 * u), new Color(0x5A5560), 0, fy, new Color(0x2E2A34)));
        g.fill(new RoundRectangle2D.Double(cx - 130 * u, fy - 230 * u, 260 * u, 235 * u, 14 * u, 14 * u));
        g.setColor(new Color(0x16091C));
        g.fill(new RoundRectangle2D.Double(cx - 78 * u, fy - 140 * u, 156 * u, 142 * u, 90 * u, 90 * u));
        glow(g, cx, fy - 40 * u, 110 * u, new Color(0xFFB347), 120);
        for(int k = -2; k <= 2; k++) {
            double hh = (60 + 26 * Math.sin(t * 6 + k * 1.7) + (2 - Math.abs(k)) * 12) * u;
            flame(g, cx + k * 24 * u, fy - 4 * u, hh, 20 * u, new Color(0xFF7A2E), new Color(0xFFD36B));
        }
        g.setColor(new Color(0x2A1A38));
        g.fill(new RoundRectangle2D.Double(cx - 60 * u, fy - 12 * u, 120 * u, 12 * u, 6 * u, 6 * u));  // log
        // mantel with candles and bottles
        g.setColor(new Color(0x8A6A4E));
        g.fill(new RoundRectangle2D.Double(cx - 160 * u, fy - 246 * u, 320 * u, 20 * u, 8 * u, 8 * u));
        for(int s = -1; s <= 1; s += 2) {
            double bx = cx + s * 110 * u;
            g.setColor(new Color(0xFFF1D4));
            g.fill(new RoundRectangle2D.Double(bx - 6 * u, fy - 290 * u, 12 * u, 44 * u, 4 * u, 4 * u));
            flame(g, bx, fy - 288 * u, (20 + 4 * Math.sin(t * 9 + s)) * u, 7 * u, new Color(0xFFB347), Color.WHITE);
            glow(g, bx, fy - 300 * u, 40 * u, new Color(0xFFD36B), 60);
        }
        g.setColor(Theme.alpha(Theme.MINT, 220));                                   // little potion bottle
        g.fill(new RoundRectangle2D.Double(cx - 8 * u, fy - 274 * u, 16 * u, 28 * u, 8 * u, 8 * u));
        g.setColor(new Color(0xFFE9B0));
        g.fill(new Rectangle2D.Double(cx - 3 * u, fy - 282 * u, 6 * u, 9 * u));
        // hanging herbs
        for(int i = 0; i < 3; i++) {
            double hx = cx + 245 * u + i * 34 * u;
            g.setColor(new Color(0x6B4A3A));
            g.draw(new Line2D.Double(hx, fy - 330 * u, hx, fy - 270 * u));
            g.setColor(i % 2 == 0 ? new Color(0x5A4A32) : new Color(0x6A3A3A));
            for(int l = 0; l < 6; l++) {
                g.fill(new Ellipse2D.Double(hx - 7 * u + (l % 2) * 6 * u, fy - 272 * u + l * 9 * u, 8 * u, 14 * u));
            }
        }
    }

    private static void kitchen(Graphics2D g, int cx, int fy, double u, double t)
    {
        // shelf with jars
        g.setColor(new Color(0x6B4A3A));
        g.fill(new RoundRectangle2D.Double(cx - 330 * u, fy - 250 * u, 660 * u, 12 * u, 6 * u, 6 * u));
        Color[] jar = { new Color(0x9C2230), new Color(0x6E8A5A), Theme.GOLD_DARK, new Color(0x5A3A6A), new Color(0xB03A3A), new Color(0x7A6A3A), Theme.ROSE_DARK };
        for(int i = 0; i < 9; i++) {
            double jx = cx - 300 * u + i * 72 * u, jh = (46 + (i * 13) % 20) * u;
            glow(g, jx + 17 * u, fy - 250 * u - jh / 2, 38 * u, jar[i % jar.length], 55);
            g.setColor(Theme.alpha(jar[i % jar.length], 215));
            g.fill(new RoundRectangle2D.Double(jx, fy - 250 * u - jh, 34 * u, jh, 12 * u, 12 * u));
            g.setColor(new Color(0x4A3A66));
            g.fill(new RoundRectangle2D.Double(jx + 4 * u, fy - 250 * u - jh - 8 * u, 26 * u, 9 * u, 4 * u, 4 * u));
            g.setColor(new Color(255, 255, 255, 90));
            g.fill(new Ellipse2D.Double(jx + 6 * u, fy - 250 * u - jh + 8 * u, 5 * u, jh * 0.4));
        }
        // the big cauldron
        glow(g, cx, fy - 70 * u, 220 * u, Theme.ROSE_DARK, 60);
        for(int k = -2; k <= 2; k++) {
            flame(g, cx + k * 28 * u, fy + 8 * u, (30 + 10 * Math.sin(t * 7 + k)) * u, 12 * u, new Color(0xFF7A2E), new Color(0xFFD36B));
        }
        g.setPaint(new GradientPaint(0, (float) (fy - 110 * u), new Color(0x4A4266), 0, (float) fy, new Color(0x1B1530)));
        g.fill(new Ellipse2D.Double(cx - 100 * u, fy - 112 * u, 200 * u, 124 * u));
        g.setColor(new Color(0x2C2546));
        g.fill(new Ellipse2D.Double(cx - 108 * u, fy - 122 * u, 216 * u, 44 * u));
        g.setPaint(new GradientPaint(0, (float) (fy - 118 * u), new Color(0xA01626), 0, (float) (fy - 84 * u), new Color(0xE0303F)));
        g.fill(new Ellipse2D.Double(cx - 92 * u, fy - 118 * u, 184 * u, 32 * u));
        for(int i = 0; i < 9; i++) {
            double ph = (t * 0.5 + i * 0.111) % 1.0;
            double bx = cx + Math.sin(i * 2.3 + t) * 60 * u, by = fy - 110 * u - ph * 150 * u;
            double r = (6 + (i % 3) * 3) * u * (1 - ph * 0.4);
            g.setColor(Theme.alpha(new Color(0xFF5060), (int) (230 * (1 - ph))));
            g.draw(new Ellipse2D.Double(bx - r, by - r, r * 2, r * 2));
            g.setColor(Theme.alpha(new Color(0xFF5060), (int) (70 * (1 - ph))));
            g.fill(new Ellipse2D.Double(bx - r, by - r, r * 2, r * 2));
        }
    }

    private static void library(Graphics2D g, int cx, int fy, double u, double t)
    {
        Color[] books = { new Color(0xB45AE8), new Color(0xE8639F), new Color(0x4F7BE8), new Color(0x3DC9A8),
                          new Color(0xE8B04F), new Color(0x8E63F0), new Color(0xE86F6F) };
        for(int side = -1; side <= 1; side += 2) {
            double sx = side < 0 ? cx - 330 * u : cx + 230 * u, sw = 150 * u, top = fy - 330 * u;
            g.setColor(new Color(0x3A2450));
            g.fill(new RoundRectangle2D.Double(sx, top, sw, 335 * u, 8 * u, 8 * u));
            Random r = new Random(side + 5);
            for(int row = 0; row < 5; row++) {
                double by = top + 12 * u + row * 64 * u;
                double bx = sx + 8 * u;
                while(bx < sx + sw - 18 * u) {
                    double bw = (10 + r.nextInt(9)) * u, bh = (38 + r.nextInt(16)) * u;
                    g.setColor(books[r.nextInt(books.length)]);
                    g.fill(new RoundRectangle2D.Double(bx, by + 50 * u - bh, bw, bh, 3 * u, 3 * u));
                    g.setColor(new Color(255, 255, 255, 60));
                    g.fill(new Rectangle2D.Double(bx + 2 * u, by + 50 * u - bh + 6 * u, bw - 4 * u, 2 * u));
                    bx += bw + 1.5 * u;
                }
                g.setColor(new Color(0x5C3C78));
                g.fill(new Rectangle2D.Double(sx + 4 * u, by + 50 * u, sw - 8 * u, 6 * u));
            }
        }
        // floating, glowing spell book
        double fyb = fy - 190 * u + Math.sin(t * 2) * 10 * u;
        glow(g, cx, fyb, 130 * u, Theme.GOLD, 70);
        g.setColor(new Color(0x2A1760));
        g.fill(new RoundRectangle2D.Double(cx - 62 * u, fyb - 34 * u, 124 * u, 70 * u, 8 * u, 8 * u));
        for(int s = -1; s <= 1; s += 2) {
            Path2D.Double page = new Path2D.Double();
            page.moveTo(cx, fyb - 30 * u);
            page.quadTo(cx + s * 30 * u, fyb - 42 * u, cx + s * 58 * u, fyb - 30 * u);
            page.lineTo(cx + s * 58 * u, fyb + 28 * u);
            page.quadTo(cx + s * 30 * u, fyb + 18 * u, cx, fyb + 30 * u);
            page.closePath();
            g.setColor(new Color(0xFFF1D4));
            g.fill(page);
            g.setColor(new Color(0xC9A877));
            for(int l = 0; l < 4; l++) {
                g.draw(new Line2D.Double(cx + s * 10 * u, fyb - 16 * u + l * 11 * u, cx + s * 48 * u, fyb - 18 * u + l * 11 * u));
            }
        }
        for(int i = 0; i < 6; i++) {
            double a = t * 1.3 + i * 1.05;
            Theme.drawSparkle(g, cx + Math.cos(a) * 90 * u, fyb + Math.sin(a * 1.3) * 50 * u, (4 + i % 3 * 2) * u, Theme.alpha(Theme.GOLD, 200));
        }
    }

    private static void tower(Graphics2D g, int cx, int fy, double u, double t)
    {
        window(g, cx - 160 * u, fy - 360 * u, 320 * u, 360 * u, t, 23);
        // curtains
        for(int side = -1; side <= 1; side += 2) {
            double ex = cx + side * 160 * u;
            Path2D.Double c = new Path2D.Double();
            c.moveTo(ex, fy - 380 * u);
            c.curveTo(ex + side * 70 * u, fy - 300 * u, ex + side * 10 * u, fy - 160 * u, ex + side * 50 * u, fy - 10 * u);
            c.lineTo(ex + side * 110 * u, fy - 10 * u);
            c.lineTo(ex + side * 110 * u, fy - 380 * u);
            c.closePath();
            g.setPaint(new GradientPaint((float) ex, 0f, new Color(0x9B3A86), (float) (ex + side * 110 * u), 0f, new Color(0x5C1F5A)));
            g.fill(c);
        }
        // telescope
        double tx = cx + 250 * u;
        g.setColor(new Color(0x8A6A4E));
        g.setStroke(new BasicStroke((float) (5 * u), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(tx, fy - 90 * u, tx - 36 * u, fy + 20 * u));
        g.draw(new Line2D.Double(tx, fy - 90 * u, tx + 36 * u, fy + 20 * u));
        g.draw(new Line2D.Double(tx, fy - 90 * u, tx, fy + 28 * u));
        AffineTransform keep = g.getTransform();
        g.rotate(-0.7, tx, fy - 90 * u);
        g.setColor(new Color(0xE0B04F));
        g.fill(new RoundRectangle2D.Double(tx - 70 * u, fy - 104 * u, 140 * u, 26 * u, 10 * u, 10 * u));
        g.setColor(new Color(0x7B5BD0));
        g.fill(new RoundRectangle2D.Double(tx - 70 * u, fy - 104 * u, 28 * u, 26 * u, 10 * u, 10 * u));
        g.setTransform(keep);
        g.setStroke(new BasicStroke(1f));
        // little crystal ball on the floor
        glow(g, cx - 250 * u, fy - 24 * u, 70 * u, Theme.ROSE, 90);
        g.setPaint(new GradientPaint((float) (cx - 270 * u), (float) (fy - 60 * u), new Color(0xFFD9F5), (float) (cx - 230 * u), (float) fy, new Color(0x8E63F0)));
        g.fill(new Ellipse2D.Double(cx - 276 * u, fy - 56 * u, 52 * u, 52 * u));
        Theme.drawSparkle(g, cx - 250 * u + Math.sin(t * 2) * 8 * u, fy - 30 * u, 6 * u, Color.WHITE);
    }

    private static void mushroom(Graphics2D g, double x, double y, double s, Color cap, double t, int i)
    {
        // in the horror castle the "mushrooms" are glowing skulls
        double pulse = 0.7 + 0.3 * Math.sin(t * 2.4 + i);
        glow(g, x, y - s * 0.6, s * 2.2, new Color(0x7FFFA0), (int) (60 * pulse));
        g.setColor(new Color(0xE9E2D0));
        g.fill(new Ellipse2D.Double(x - s * 0.55, y - s * 1.25, s * 1.1, s * 0.95));
        g.fill(new RoundRectangle2D.Double(x - s * 0.32, y - s * 0.55, s * 0.64, s * 0.5, s * 0.15, s * 0.15));
        g.setColor(new Color(0x120A0E));
        g.fill(new Ellipse2D.Double(x - s * 0.38, y - s * 0.98, s * 0.28, s * 0.3));
        g.fill(new Ellipse2D.Double(x + s * 0.1, y - s * 0.98, s * 0.28, s * 0.3));
        Path2D.Double nose = new Path2D.Double();
        nose.moveTo(x, y - s * 0.72); nose.lineTo(x - s * 0.08, y - s * 0.55); nose.lineTo(x + s * 0.08, y - s * 0.55); nose.closePath();
        g.fill(nose);
        for(int k = -2; k <= 2; k++) {
            g.fill(new Rectangle2D.Double(x + k * s * 0.12 - s * 0.03, y - s * 0.5, s * 0.06, s * 0.3));
        }
        g.setColor(Theme.alpha(new Color(0x7FFFA0), (int) (180 * pulse)));
        g.fill(new Ellipse2D.Double(x - s * 0.3, y - s * 0.93, s * 0.16, s * 0.16));
        g.fill(new Ellipse2D.Double(x + s * 0.16, y - s * 0.93, s * 0.16, s * 0.16));
    }

    private static void cellar(Graphics2D g, int cx, int fy, double u, double t)
    {
        // stone archway
        g.setColor(new Color(0x0A1520));
        g.fill(new RoundRectangle2D.Double(cx - 110 * u, fy - 250 * u, 220 * u, 255 * u, 200 * u, 200 * u));
        g.setColor(new Color(0x3B5568));
        g.setStroke(new BasicStroke((float) (9 * u)));
        g.draw(new RoundRectangle2D.Double(cx - 110 * u, fy - 250 * u, 220 * u, 255 * u, 200 * u, 200 * u));
        g.setStroke(new BasicStroke(1f));
        // barrels
        for(int i = 0; i < 2; i++) {
            double bx = cx - 360 * u + i * 90 * u, by = fy + (i * 14) * u;
            g.setPaint(new GradientPaint((float) bx, 0f, new Color(0x6B4A3A), (float) (bx + 70 * u), 0f, new Color(0x3E2A24)));
            g.fill(new RoundRectangle2D.Double(bx, by - 110 * u, 70 * u, 112 * u, 30 * u, 30 * u));
            g.setColor(new Color(0x9AA3B8));
            g.fill(new Rectangle2D.Double(bx + 1 * u, by - 80 * u, 68 * u, 5 * u));
            g.fill(new Rectangle2D.Double(bx + 1 * u, by - 35 * u, 68 * u, 5 * u));
        }
        // glowing mushrooms
        double[][] spots = { { -260, 20, 30 }, { -210, 60, 20 }, { 230, 0, 34 }, { 290, 40, 22 }, { 180, 70, 16 },
                             { -120, 120, 18 }, { 120, 150, 24 }, { 340, 150, 14 } };
        Color[] caps = { Theme.TEAL, Theme.ROSE, Theme.PURPLE, Theme.MINT };
        for(int i = 0; i < spots.length; i++) {
            mushroom(g, cx + spots[i][0] * u, fy + spots[i][1] * u + 40 * u, spots[i][2] * u * 1.6, caps[i % caps.length], t, i);
        }
        for(int i = 0; i < 4; i++) {
            mushroom(g, cx - 300 * u + i * 200 * u, fy - 200 * u - (i % 2) * 40 * u, 16 * u, caps[(i + 1) % caps.length], t, i + 9);
        }
    }

    private static void portal(Graphics2D g, int cx, int fy, double u, double t)
    {
        double cy = fy - 150 * u, R = 150 * u;
        glow(g, cx, cy, R * 2.3, Theme.PURPLE, 100);
        g.setColor(new Color(0x4A3A80));
        g.fill(new Ellipse2D.Double(cx - R - 18 * u, cy - R - 18 * u, 2 * R + 36 * u, 2 * R + 36 * u));
        float[] f = { 0f, 0.45f, 1f };
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) R, f,
                   new Color[] { new Color(0xFFE6FB), new Color(0xB07BFF), new Color(0x3A1A80) }));
        g.fill(new Ellipse2D.Double(cx - R, cy - R, 2 * R, 2 * R));
        Shape clip = g.getClip();
        g.clip(new Ellipse2D.Double(cx - R, cy - R, 2 * R, 2 * R));
        Color[] sw = { Theme.ROSE, Theme.PURPLE, Theme.TEAL, Color.WHITE };
        for(int i = 0; i < 7; i++) {
            double rr = R * (0.2 + i * 0.12);
            g.setStroke(new BasicStroke((float) (6 * u), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Theme.alpha(sw[i % sw.length], 170));
            g.draw(new Arc2D.Double(cx - rr, cy - rr, 2 * rr, 2 * rr, (t * (40 + i * 18)) * (i % 2 == 0 ? 1 : -1), 110, Arc2D.OPEN));
        }
        g.setStroke(new BasicStroke(1f));
        g.setClip(clip);
        for(int i = 0; i < 12; i++) {                                         // golden runes on the ring
            double a = i * Math.PI / 6 + t * 0.2;
            double rx = cx + Math.cos(a) * (R + 9 * u), ry = cy + Math.sin(a) * (R + 9 * u);
            Theme.drawSparkle(g, rx, ry, 5 * u, Theme.alpha(Theme.GOLD, (int) (150 + 100 * Math.sin(t * 3 + i))));
        }
        g.setColor(Theme.alpha(Theme.GOLD, 120));                             // rune circle on the floor
        g.setStroke(new BasicStroke((float) (2.5 * u)));
        g.draw(new Ellipse2D.Double(cx - 200 * u, fy + 90 * u, 400 * u, 100 * u));
        g.draw(new Ellipse2D.Double(cx - 150 * u, fy + 105 * u, 300 * u, 70 * u));
        g.setStroke(new BasicStroke(1f));
    }

    private static void garden(Graphics2D g, int w, int cx, int fy, double u, double t)
    {
        moon(g, cx + 230 * u, fy - 250 * u, 62 * u);
        // dark hills
        for(int layer = 0; layer < 2; layer++) {
            Path2D.Double hill = new Path2D.Double();
            hill.moveTo(0, fy);
            for(int x = 0; x <= w; x += 40) {
                hill.lineTo(x, fy - (30 + layer * 22) * u - Math.sin(x * 0.006 + layer * 2) * 26 * u);
            }
            hill.lineTo(w, fy);
            hill.closePath();
            g.setColor(layer == 0 ? new Color(0x153A33) : new Color(0x0F2A2A));
            g.fill(hill);
        }
        // the garden gate (the way out!)
        g.setColor(new Color(0x4A5470));
        g.fill(new RoundRectangle2D.Double(cx - 150 * u, fy - 190 * u, 40 * u, 200 * u, 8 * u, 8 * u));
        g.fill(new RoundRectangle2D.Double(cx + 110 * u, fy - 190 * u, 40 * u, 200 * u, 8 * u, 8 * u));
        g.setColor(Theme.GOLD);
        g.fill(new Ellipse2D.Double(cx - 142 * u, fy - 212 * u, 24 * u, 24 * u));
        g.fill(new Ellipse2D.Double(cx + 118 * u, fy - 212 * u, 24 * u, 24 * u));
        g.setColor(new Color(0x2A2F45));
        g.setStroke(new BasicStroke((float) (5 * u)));
        for(int i = 0; i <= 9; i++) {
            double bx = cx - 100 * u + i * 22 * u;
            double top = fy - 150 * u - Math.sin(i / 9.0 * Math.PI) * 40 * u;
            g.draw(new Line2D.Double(bx, fy, bx, top));
            Theme.drawStar(g, bx, top - 6 * u, 6 * u, new Color(0x2A2F45));
        }
        g.setStroke(new BasicStroke(1f));
        glow(g, cx, fy - 80 * u, 260 * u, Theme.GOLD, 40);
        // tombstones and dead trees
        Random r = new Random(4);
        for(int i = 0; i < 16; i++) {
            double tx = r.nextDouble() * w, ty = fy + 30 * u + r.nextDouble() * 250 * u;
            double tw = (26 + r.nextInt(18)) * u, th = (40 + r.nextInt(26)) * u;
            g.setColor(new Color(0, 0, 0, 90));
            g.fill(new Ellipse2D.Double(tx - tw * 0.6, ty - 4 * u, tw * 1.2, 10 * u));
            g.setPaint(new GradientPaint((float) tx, (float) (ty - th), new Color(0x7A7F8C), (float) tx, (float) ty, new Color(0x3A3E4A)));
            Path2D.Double stone = new Path2D.Double();
            stone.moveTo(tx - tw / 2, ty);
            stone.lineTo(tx - tw / 2, ty - th + tw / 2);
            stone.quadTo(tx - tw / 2, ty - th, tx, ty - th);
            stone.quadTo(tx + tw / 2, ty - th, tx + tw / 2, ty - th + tw / 2);
            stone.lineTo(tx + tw / 2, ty);
            stone.closePath();
            g.fill(stone);
            g.setColor(new Color(0x2A2D38));
            g.setStroke(new BasicStroke((float) (2.4 * u)));
            g.draw(new Line2D.Double(tx, ty - th * 0.78, tx, ty - th * 0.4));
            g.draw(new Line2D.Double(tx - tw * 0.22, ty - th * 0.64, tx + tw * 0.22, ty - th * 0.64));
            g.setStroke(new BasicStroke(1f));
        }
        g.setColor(new Color(0x0B0608));                                  // a dead tree
        g.setStroke(new BasicStroke((float) (10 * u), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double trx = cx - 300 * u;
        g.draw(new Line2D.Double(trx, fy + 20 * u, trx + 8 * u, fy - 150 * u));
        g.setStroke(new BasicStroke((float) (5 * u), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(trx + 8 * u, fy - 110 * u, trx - 60 * u, fy - 170 * u));
        g.draw(new Line2D.Double(trx + 8 * u, fy - 90 * u, trx + 70 * u, fy - 160 * u));
        g.draw(new Line2D.Double(trx + 6 * u, fy - 150 * u, trx - 20 * u, fy - 210 * u));
        g.setStroke(new BasicStroke(1f));
    }
}
