import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

/**
 * Draws the cottage as a cute map. Only rooms the player has visited (and their
 * direct neighbours, shown as "?") are revealed. Willow the little witch walks from
 * room to room; when she is teleported (beamer or portal) she vanishes in a
 * cloud of sparkles and pops up in the new room.
 */
public class MapPanel extends JComponent
{
    private Game game;
    private final HashMap<String, Point> grid = new HashMap<String, Point>();
    private final HashMap<String, Color> roomColors = new HashMap<String, Color>();
    private final HashMap<String, String> shortNames = new HashMap<String, String>();
    private float phase = 0f;
    private double time = 0;

    // ---- hero animation state (positions are in grid units: 1.0 = one room)
    private Room avatarRoom;
    private double gx, gy, tx, ty;
    private double walkPhase, moveAmt, facing, offset;
    private boolean walking;
    private int teleState;               // 0 = none, 1 = vanishing, 2 = appearing
    private double teleScale = 1;
    private double hopT = -1;
    private int celebrate;

    private final ArrayList<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random();
    private final double[] starX = new double[18], starY = new double[18], starP = new double[18], starS = new double[18];

    private static class Particle
    {
        double x, y, vx, vy, life, size;
        int kind;                         // 0 sparkle, 1 heart, 2 star
        Color color;
    }

    public MapPanel()
    {
        setOpaque(false);
        grid.put("Dungeon", new Point(0, 0));
        grid.put("Forbidden Library", new Point(1, 0));
        grid.put("Bell Tower", new Point(2, 0));
        grid.put("Cursed Portal", new Point(0, 1));
        grid.put("Great Hall", new Point(1, 1));
        grid.put("Cursed Kitchen", new Point(2, 1));
        grid.put("Graveyard Gate", new Point(1, 2));

        shortNames.put("Dungeon", "Dungeon");
        shortNames.put("Forbidden Library", "Library");
        shortNames.put("Bell Tower", "Tower");
        shortNames.put("Cursed Portal", "Portal");
        shortNames.put("Great Hall", "Hall");
        shortNames.put("Cursed Kitchen", "Kitchen");
        shortNames.put("Graveyard Gate", "Graveyard");

        roomColors.put("Great Hall", new Color(0x5A1A26));
        roomColors.put("Cursed Kitchen", new Color(0x4A3A2A));
        roomColors.put("Forbidden Library", new Color(0x2A2F4A));
        roomColors.put("Bell Tower", new Color(0x3A3040));
        roomColors.put("Dungeon", new Color(0x24343A));
        roomColors.put("Cursed Portal", new Color(0x6A1A3A));
        roomColors.put("Graveyard Gate", new Color(0x2A4A32));

        Random r = new Random(7);
        for(int i = 0; i < starX.length; i++) {
            starX[i] = r.nextDouble();
            starY[i] = r.nextDouble();
            starP[i] = r.nextDouble() * 6.3;
            starS[i] = 2 + r.nextDouble() * 3;
        }
        new javax.swing.Timer(33, e -> tick()).start();
    }

    public void setGame(Game game)
    {
        this.game = game;
        avatarRoom = null;               // snap the hero to the start room
        particles.clear();
        teleState = 0;
        teleScale = 1;
        celebrate = 0;
        repaint();
    }

    // ------------------------------------------------------------------ animation

    private double cw, ch, nw, nh;
    private static final int PAD = 12;

    private void updateMetrics()
    {
        int w = getWidth(), h = getHeight();
        cw = (w - 2 * PAD) / 3.0;
        ch = (h - 2 * PAD - 20) / 3.0;
        nw = Math.min(cw - 10, 104);
        nh = Math.min(ch - 18, 46);
    }

    private double pixelX(double g) { return PAD + cw * (g + 0.5); }
    private double pixelY(double g) { return PAD + ch * (g + 0.5); }

    private void tick()
    {
        time += 0.033;
        phase += 0.1f;
        if(game != null) {
            updateMetrics();
            syncHero();
            stepHero();
            stepParticles();
        }
        if(isShowing()) {
            repaint();
        }
    }

    /** Notice that the player changed room and decide: walk or teleport? */
    private void syncHero()
    {
        Room cur = game.getPlayer().getCurrentRoom();
        Point p = grid.get(cur.getName());
        if(p == null) {
            return;
        }
        if(avatarRoom == null) {
            gx = tx = p.x;
            gy = ty = p.y;
        }
        else if(cur != avatarRoom) {
            tx = p.x;
            ty = p.y;
            boolean adjacent = avatarRoom.getExits().containsValue(cur) || cur.getExits().containsValue(avatarRoom);
            teleState = adjacent ? 0 : 1;
        }
        avatarRoom = cur;
    }

    private void stepHero()
    {
        if(teleState == 1) {                                  // vanishing
            moveAmt = 0;
            teleScale -= 0.09;
            if(teleScale <= 0) {
                teleScale = 0;
                burst(pixelX(gx), pixelY(gy) - 20, 9, 0);
                gx = tx;
                gy = ty;
                burst(pixelX(gx), pixelY(gy) - 20, 9, 2);
                teleState = 2;
            }
            return;
        }
        if(teleState == 2) {                                  // appearing
            teleScale += 0.09;
            if(teleScale >= 1) {
                teleScale = 1;
                teleState = 0;
                hopT = 0;
            }
            return;
        }
        double dx = tx - gx, dy = ty - gy, d = Math.hypot(dx, dy);
        if(d > 0.012) {
            double step = Math.min(d, 0.05);
            gx += dx / d * step;
            gy += dy / d * step;
            walkPhase += 0.3;
            moveAmt = Math.min(1, moveAmt + 0.2);
            facing += (dx / d - facing) * 0.3;
            walking = true;
        }
        else {
            if(walking) {                                     // just arrived
                walking = false;
                gx = tx;
                gy = ty;
                hopT = 0;
                burst(pixelX(gx), pixelY(gy) - nh / 2, 4, 1);
            }
            moveAmt = Math.max(0, moveAmt - 0.2);
            facing *= 0.8;
        }
        if(hopT >= 0) {
            hopT += 0.08;
            if(hopT >= 1) {
                hopT = -1;
            }
        }
        boolean here = !game.getPlayer().getCurrentRoom().getCharacters().isEmpty();
        double target = here ? -22 : 0;
        offset += (target - offset) * 0.2;
        if(game.isFinished() && game.getOutcome() == Game.Outcome.WON && ++celebrate % 7 == 0) {
            burst(pixelX(gx) + offset, pixelY(gy) - nh / 2 - 30, 1, 1);
        }
    }

    private void burst(double x, double y, int count, int kind)
    {
        Color[] palette = { Theme.ROSE, Theme.LAVENDER, new Color(0xFFD27A), Theme.MINT, Color.WHITE };
        for(int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = x + random.nextGaussian() * 5;
            p.y = y + random.nextGaussian() * 5;
            double a = random.nextDouble() * Math.PI * 2;
            double sp = 0.6 + random.nextDouble() * 1.6;
            p.vx = Math.cos(a) * sp;
            p.vy = Math.sin(a) * sp - 1.0;
            p.life = 1;
            p.size = 5 + random.nextDouble() * 5;
            p.kind = (kind == 0 && random.nextInt(3) == 0) ? 1 : kind;
            p.color = palette[random.nextInt(palette.length)];
            if(p.kind == 1) {
                p.color = random.nextBoolean() ? Theme.ROSE : Theme.ROSE_DARK;
            }
            particles.add(p);
        }
    }

    private void stepParticles()
    {
        for(Iterator<Particle> it = particles.iterator(); it.hasNext(); ) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vx *= 0.96;
            p.vy = p.vy * 0.96 - 0.02;
            p.life -= 0.022;
            if(p.life <= 0) {
                it.remove();
            }
        }
    }

    // ------------------------------------------------------------------ drawing

    private Point2D.Double center(Room r)
    {
        Point p = grid.get(r.getName());
        if(p == null) {
            return null;
        }
        return new Point2D.Double(PAD + cw * p.x + cw / 2, PAD + ch * p.y + ch / 2);
    }

    private boolean lockedBetween(Room a, Room b)
    {
        for(Map.Entry<String, Room> e : a.getExits().entrySet()) {
            if(e.getValue() == b && a.isLocked(e.getKey())) {
                return true;
            }
        }
        for(Map.Entry<String, Room> e : b.getExits().entrySet()) {
            if(e.getValue() == a && b.isLocked(e.getKey())) {
                return true;
            }
        }
        return false;
    }

    protected void paintComponent(Graphics g0)
    {
        if(game == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        int w = getWidth(), h = getHeight();
        updateMetrics();

        // twinkling background stars
        for(int i = 0; i < starX.length; i++) {
            double tw = 0.5 + 0.5 * Math.sin(time * 2 + starP[i]);
            Color c = (i % 3 == 0) ? Theme.ROSE : (i % 3 == 1) ? Theme.LAVENDER : new Color(0xFFD27A);
            Theme.drawSparkle(g, w * starX[i], (h - 20) * starY[i], starS[i] * (0.6 + 0.6 * tw), Theme.alpha(c, (int) (70 + 100 * tw)));
        }

        Player player = game.getPlayer();
        HashSet<Room> visible = new HashSet<Room>();
        for(Room r : game.getAllRooms()) {
            if(player.hasVisited(r)) {
                visible.add(r);
                visible.addAll(r.getExits().values());
            }
        }

        // connections: dotted paths
        for(Room r : game.getAllRooms()) {
            if(!player.hasVisited(r)) {
                continue;
            }
            for(Map.Entry<String, Room> e : r.getExits().entrySet()) {
                Room t = e.getValue();
                Point2D.Double a = center(r), b = center(t);
                if(a == null || b == null) {
                    continue;
                }
                boolean reverse = t.getExits().containsValue(r);
                if(reverse && player.hasVisited(t) && r.getName().compareTo(t.getName()) > 0) {
                    continue;                                   // draw two-way links only once
                }
                boolean vertical = e.getKey().equals("up") || e.getKey().equals("down");
                Stroke path = vertical
                    ? new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] { 0.1f, 8f }, 0f)
                    : new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] { 0.1f, 9f }, 0f);
                g.setStroke(path);
                g.setColor(Theme.alpha(vertical ? Theme.PURPLE : Theme.ROSE, 190));
                g.draw(new Line2D.Double(a, b));
                if(!reverse) {
                    drawArrow(g, a, b);
                }
                if(lockedBetween(r, t)) {
                    double mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2;
                    g.setStroke(new BasicStroke(1f));
                    g.setColor(new Color(0, 0, 0, 70));
                    g.fill(new Ellipse2D.Double(mx - 12, my - 9, 24, 24));
                    g.setColor(Theme.PANEL);
                    g.fill(new Ellipse2D.Double(mx - 12, my - 12, 24, 24));
                    g.setColor(Theme.alpha(Theme.GOLD, 200));
                    g.draw(new Ellipse2D.Double(mx - 12, my - 12, 24, 24));
                    Theme.drawLock(g, (int) mx, (int) my + 2, Theme.GOLD);
                }
            }
        }
        g.setStroke(new BasicStroke(1f));

        // rooms
        for(Room r : game.getAllRooms()) {
            if(!visible.contains(r)) {
                continue;
            }
            Point2D.Double c = center(r);
            if(c == null) {
                continue;
            }
            drawRoom(g, r, c, player);
        }

        // the hero and her friends
        drawActors(g);

        // sparkles and hearts
        for(Particle p : particles) {
            int a = (int) (255 * Math.max(0, Math.min(1, p.life)));
            double sz = p.size * (0.5 + 0.5 * p.life);
            if(p.kind == 1) {
                Theme.drawHeart(g, p.x, p.y, sz * 1.5, Theme.alpha(p.color, a));
            }
            else if(p.kind == 2) {
                Theme.drawStar(g, p.x, p.y, sz * 0.8, Theme.alpha(p.color, a));
            }
            else {
                Theme.drawSparkle(g, p.x, p.y, sz, Theme.alpha(p.color, a));
            }
        }

        g.setFont(Theme.ui(Font.PLAIN, 10));
        g.setColor(Theme.MUTED);
        String legend = "dotted: up/down   arrow: one-way   lock: locked";
        g.drawString(legend, (w - g.getFontMetrics().stringWidth(legend)) / 2, h - 4);
        g.dispose();
    }

    /** Rooms are drawn as little tombstones with a pointed arch on top. */
    private Path2D.Double tombstone(double x, double y, double w, double h)
    {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(x, y + h);
        p.lineTo(x, y + h * 0.40);
        p.quadTo(x, y + h * 0.05, x + w / 2, y - h * 0.08);
        p.quadTo(x + w, y + h * 0.05, x + w, y + h * 0.40);
        p.lineTo(x + w, y + h);
        p.closePath();
        return p;
    }

    private void drawRoom(Graphics2D g, Room r, Point2D.Double c, Player player)
    {
        double x = c.x - nw / 2, y = c.y - nh / 2;
        Path2D.Double box = tombstone(x, y, nw, nh);
        boolean visited = player.hasVisited(r);
        boolean current = r == player.getCurrentRoom();
        Color pastel = roomColors.containsKey(r.getName()) ? roomColors.get(r.getName()) : Theme.PANEL_LIGHT;
        Color textColor;

        // soft shadow under every room
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new RoundRectangle2D.Double(x + 1, y + 4, nw, nh, 22, 22));

        if(current) {
            for(int i = 4; i >= 1; i--) {
                int a = (int) ((26 + 18 * Math.sin(phase)) / i);
                g.setColor(Theme.alpha(Theme.ROSE, a));
                g.fill(new RoundRectangle2D.Double(x - i * 3, y - i * 3, nw + i * 6, nh + i * 6, 22 + i * 5, 22 + i * 5));
            }
            g.setPaint(new GradientPaint(0, (float) y, new Color(0xFFE9B0), 0, (float) (y + nh), new Color(0xF7B955)));
            g.fill(box);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.2f));
            g.draw(box);
            g.setStroke(new BasicStroke(1f));
            textColor = new Color(0x5A3A10);
        }
        else if(visited) {
            g.setPaint(new GradientPaint(0, (float) y, Theme.blend(pastel, Color.WHITE, 0.14f), 0, (float) (y + nh), pastel));
            g.fill(box);
            g.setColor(Theme.blend(pastel, Color.WHITE, 0.38f));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(box);
            g.setStroke(new BasicStroke(1f));
            Theme.drawHeart(g, x + 12, y + 10, 8, Theme.alpha(Theme.ROSE, 190));
            textColor = Theme.TEXT;
        }
        else {
            g.setColor(new Color(255, 255, 255, 16));
            g.fill(box);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] { 4f, 5f }, 0f));
            g.setColor(Theme.alpha(Theme.LAVENDER, 230));
            g.draw(box);
            g.setStroke(new BasicStroke(1f));
            textColor = Theme.alpha(Theme.LAVENDER, 255);
        }
        String label = visited ? (shortNames.containsKey(r.getName()) ? shortNames.get(r.getName()) : r.getName()) : "?";
        int fontSize = 12;
        g.setFont(Theme.ui(Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        while(fm.stringWidth(label) > nw - 8 && fontSize > 9) {      // long names shrink to fit
            fontSize--;
            g.setFont(Theme.ui(Font.BOLD, fontSize));
            fm = g.getFontMetrics();
        }
        g.setColor(textColor);
        g.drawString(label, (float) (c.x - fm.stringWidth(label) / 2.0),
                     (float) (c.y + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    private void drawActors(Graphics2D g)
    {
        if(avatarRoom == null) {
            return;
        }
        double s = Math.max(0.42, Math.min(0.7, ch / 130.0));
        double px = pixelX(gx) + offset * s;
        double feet = pixelY(gy) - nh / 2 + 9;

        // friends standing in the same room (ghost, wizard...) appear when the hero has arrived
        if(teleState == 0 && moveAmt < 0.1) {
            Point2D.Double c = center(avatarRoom);
            if(c != null) {
                int i = 0;
                for(GameCharacter gc : avatarRoom.getCharacters()) {
                    double fx = c.x + (26 + i * 26) * s;
                    if(gc.isMoving()) {
                        Avatar.drawGhost(g, fx, feet + 2, s * 1.05, time + i);
                    }
                    else {
                        Avatar.drawCat(g, fx, feet + 2, s * 1.0, time + i);
                    }
                    i++;
                }
            }
        }

        Avatar.Mood mood = Avatar.Mood.NORMAL;
        if(game.isFinished()) {
            mood = game.getOutcome() == Game.Outcome.WON ? Avatar.Mood.HAPPY
                 : game.getOutcome() == Game.Outcome.LOST ? Avatar.Mood.SAD : Avatar.Mood.WAVE;
        }
        double hop = hopT >= 0 ? Math.sin(hopT * Math.PI) * 6 * s : 0;
        Avatar.drawHero(g, px, feet, s * (0.35 + 0.65 * teleScale), walkPhase, moveAmt, facing, time, mood,
                        hop, teleScale);
    }

    /** Arrow head just outside the target room box. */
    private void drawArrow(Graphics2D g, Point2D.Double from, Point2D.Double to)
    {
        double dx = to.x - from.x, dy = to.y - from.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if(len == 0) {
            return;
        }
        double ux = dx / len, uy = dy / len;
        double tx = ux == 0 ? Double.MAX_VALUE : (nw / 2) / Math.abs(ux);
        double ty = uy == 0 ? Double.MAX_VALUE : (nh / 2) / Math.abs(uy);
        double edge = Math.min(tx, ty) + 8;
        double px = to.x - ux * edge, py = to.y - uy * edge;
        Path2D.Double head = new Path2D.Double();
        head.moveTo(px, py);
        head.lineTo(px - ux * 12 - uy * 6, py - uy * 12 + ux * 6);
        head.lineTo(px - ux * 12 + uy * 6, py - uy * 12 - ux * 6);
        head.closePath();
        g.setStroke(new BasicStroke(1f));
        g.setColor(Theme.ROSE_DARK);
        g.fill(head);
    }
}
