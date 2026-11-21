import java.awt.*;
import java.awt.geom.*;

/**
 * Draws the little characters of the game (Willow the witch, the ghost and the
 * cat) with Java2D. Nothing here knows about the game rules: it is pure drawing,
 * so the witch can be shown in the scene, on the map and on the start screen.
 */
public final class Avatar
{
    private Avatar() { }

    /** How the witch feels: this changes the face and the pose. */
    public enum Mood { NORMAL, HAPPY, SAD, WAVE }

    private static final Color SKIN = new Color(0xFFE2D2);
    private static final Color SKIN_SHADE = new Color(0xF3C9B8);
    private static final Color HAIR = new Color(0x3A1F5C);
    private static final Color HAIR_LIGHT = new Color(0x6A3FA0);
    private static final Color DRESS_TOP = new Color(0x8E63F0);
    private static final Color DRESS_BOTTOM = new Color(0x4B2A9E);
    private static final Color CAPE = new Color(0x2A1760);
    private static final Color HAT_TOP = new Color(0x5B34C4);
    private static final Color HAT_BOTTOM = new Color(0x2E1A6E);
    private static final Color CREAM = new Color(0xFFF7EC);
    private static final Color BOOT = new Color(0x2A1A44);
    private static final Color GOLDY = new Color(0xFFD36B);
    private static final Color EYE = new Color(0x2B1A44);

    /**
     * Draw Willow the little witch.
     *
     * @param x      horizontal centre in pixels
     * @param feetY  y coordinate of the ground under the feet
     * @param s      scale (1.0 is about 66 pixels tall without the hat, 96 with it)
     * @param walk   walking phase in radians (advance it while moving)
     * @param move   0 = standing still ... 1 = walking at full swing
     * @param facing -1 = looking left, 0 = front, +1 = looking right
     * @param t      time in seconds (idle breathing, blinking, waving)
     * @param mood   facial expression and pose
     * @param extraLift extra upward offset in pixels (e.g. an arrival hop)
     * @param alpha  1 = solid, 0 = invisible
     */
    public static void drawHero(Graphics2D g0, double x, double feetY, double s, double walk, double move,
                                double facing, double t, Mood mood, double extraLift, double alpha)
    {
        if(alpha <= 0.01 || s <= 0.01) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        g.translate(x, feetY);
        g.scale(s, s);
        if(alpha < 1) {
            g.setComposite(AlphaComposite.SrcOver.derive((float) alpha));
        }

        double swing = Math.sin(walk) * move;
        double bounce = -Math.abs(Math.sin(walk)) * 3.4 * move;
        double breathe = Math.sin(t * 2.6) * 0.9 * (1 - move);
        double jump = mood == Mood.HAPPY ? -Math.abs(Math.sin(t * 5.5)) * 9 : 0;
        double lift = bounce + breathe + jump - extraLift;

        // soft ground shadow (shrinks while jumping)
        double shrink = Math.max(0.6, 1 + (jump - extraLift) / 40.0);
        g.setColor(new Color(0, 0, 0, 80));
        g.fill(new Ellipse2D.Double(-15 * shrink, -3.5, 30 * shrink, 7));

        g.rotate(facing * 0.05 * move);
        g.translate(0, lift);

        double sway = Math.sin(t * 3.0 + walk * 0.5);
        boolean sad = mood == Mood.SAD;

        // ---- cape (behind everything)
        double flap = (move > 0.05 ? Math.sin(walk + 1.0) * 4 * move : sway * 1.4) - facing * 3 * move;
        Path2D.Double cape = new Path2D.Double();
        cape.moveTo(-9, -36);
        cape.quadTo(-16 + flap, -20, -17 + flap, -7);
        cape.quadTo(0, -3, 17 + flap, -7);
        cape.quadTo(16 + flap, -20, 9, -36);
        cape.closePath();
        g.setPaint(new GradientPaint(0, -36, CAPE, 0, -5, new Color(0x170A3A)));
        g.fill(cape);
        g.setColor(new Color(GOLDY.getRed(), GOLDY.getGreen(), GOLDY.getBlue(), 200));
        Theme.drawStar(g, -9 + flap * 0.8, -12, 2.2, GOLDY);
        Theme.drawStar(g, 10 + flap * 0.8, -17, 1.8, GOLDY);

        // ---- back hair (behind the head)
        g.setColor(HAIR);
        g.fill(new Ellipse2D.Double(-17, -62, 34, 32));
        double lock = (move > 0.05 ? Math.sin(walk + 1.2) * 2.5 * move : sway * 1.1);
        for(int side = -1; side <= 1; side += 2) {
            Path2D.Double l = new Path2D.Double();
            double sx = side * 15, sy = -52;
            l.moveTo(sx - side * 3, sy);
            l.curveTo(sx + side * 3 + lock * side, sy + 6, sx + side * 4 + lock, sy + 14, sx + side * 0.5 + lock * 1.3, sy + 19);
            l.curveTo(sx - side * 5, sy + 13, sx - side * 6, sy + 6, sx - side * 3, sy);
            g.setColor(HAIR);
            g.fill(l);
        }

        // ---- legs: striped stockings and little boots
        for(int side = -1; side <= 1; side += 2) {
            double step = swing * 5.5 * side;
            double up = Math.max(0, Math.sin(walk + (side > 0 ? 0 : Math.PI))) * 3 * move;
            double fx = side * 4.5 + step * 0.35, fy = -2.5 - up;
            double hx = side * 4.5, hy = -15;
            g.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            for(int k = 0; k < 4; k++) {
                double a = k / 4.0, b = (k + 1) / 4.0;
                g.setColor(k % 2 == 0 ? Theme.ROSE : new Color(0x3A2A7A));
                g.draw(new Line2D.Double(hx + (fx - hx) * a, hy + (fy - 2 - hy) * a, hx + (fx - hx) * b, hy + (fy - 2 - hy) * b));
            }
            g.setColor(BOOT);
            g.fill(new RoundRectangle2D.Double(fx - 5.2, fy - 1.5, 10.4, 6, 5, 5));
            g.setColor(GOLDY);
            g.fill(new Ellipse2D.Double(fx - 1.2, fy - 0.2, 2.4, 2.4));
        }
        g.setStroke(new BasicStroke(1f));

        // ---- back arm
        drawArm(g, -1, swing, t, mood);

        // ---- dress
        Path2D.Double dress = new Path2D.Double();
        dress.moveTo(-8.5, -35);
        dress.quadTo(-9.5, -27, -14, -13);
        dress.quadTo(0, -9.5, 14, -13);
        dress.quadTo(9.5, -27, 8.5, -35);
        dress.quadTo(0, -37.5, -8.5, -35);
        dress.closePath();
        g.setPaint(new GradientPaint(0, -36, DRESS_TOP, 0, -10, DRESS_BOTTOM));
        g.fill(dress);
        g.setColor(new Color(255, 255, 255, 70));                       // highlight
        g.fill(new Ellipse2D.Double(-6.5, -33, 4, 10));
        g.setColor(GOLDY);                                              // scalloped gold hem
        for(int i = -3; i <= 3; i++) {
            g.fill(new Ellipse2D.Double(i * 3.8 - 1.2, -13.3 + Math.abs(i) * 0.25 - 1.2 + (3 - Math.abs(i)) * 0.5, 2.4, 2.4));
        }
        g.setColor(CREAM);                                              // round collar
        g.fill(new Ellipse2D.Double(-6, -37.5, 12, 6));
        // crescent-moon brooch
        Area moon = new Area(new Ellipse2D.Double(-3.4, -31.5, 6.8, 6.8));
        moon.subtract(new Area(new Ellipse2D.Double(-1.8, -32.2, 6.2, 6.2)));
        g.setColor(GOLDY);
        g.fill(moon);
        g.setColor(Theme.ROSE);                                         // belt
        g.fill(new RoundRectangle2D.Double(-9.5, -23.5, 19, 2.6, 2, 2));

        // ---- front arm
        drawArm(g, 1, swing, t, mood);

        // ---- head
        g.setColor(new Color(0, 0, 0, 14));
        g.fill(new Ellipse2D.Double(-6, -35.5, 12, 4));                 // neck shadow
        g.setColor(SKIN);
        g.fill(new Ellipse2D.Double(-15.5, -62, 31, 28));
        g.setColor(SKIN_SHADE);
        g.setStroke(new BasicStroke(0.8f));
        g.draw(new Arc2D.Double(-15.5, -62, 31, 28, 205, 130, Arc2D.OPEN));
        g.setStroke(new BasicStroke(1f));

        // face
        double look = facing * 1.6;
        double blinkCycle = t % 3.6;
        boolean blink = blinkCycle > 3.45;
        double eyeH = blink ? 0.7 : (sad ? 5.4 : 6.2);
        for(int side = -1; side <= 1; side += 2) {
            double ex = side * 6 + look;
            g.setColor(EYE);
            if(mood == Mood.HAPPY && !blink) {                          // happy "^ ^" eyes
                g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(ex - 2.8, -49.5, 5.6, 5.6, 20, 140, Arc2D.OPEN));
                g.setStroke(new BasicStroke(1f));
            }
            else {
                g.fill(new Ellipse2D.Double(ex - 3.1, -47 + (sad ? 1 : 0) - eyeH / 2 + 2.9, 6.2, eyeH));
                if(!blink) {
                    g.setColor(Color.WHITE);
                    g.fill(new Ellipse2D.Double(ex - 1.8, -47 + (sad ? 1 : 0) - eyeH / 2 + 3.2, 2.5, 2.5));
                    g.fill(new Ellipse2D.Double(ex + 0.7, -47 + (sad ? 1 : 0) + eyeH / 2 - 1.4, 1.3, 1.3));
                }
            }
            g.setColor(new Color(255, 110, 170, mood == Mood.HAPPY ? 160 : 115));
            g.fill(new Ellipse2D.Double(side * 10.5 + look * 0.6 - 3.8, -44, 7.6, 4.4));
        }
        g.setColor(new Color(0xB5546F));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double mx = look * 0.6;
        if(mood == Mood.HAPPY) {
            Path2D.Double mouth = new Path2D.Double();
            mouth.moveTo(mx - 3.4, -42.2);
            mouth.quadTo(mx, -36.5, mx + 3.4, -42.2);
            mouth.closePath();
            g.setColor(new Color(0xC2486A));
            g.fill(mouth);
            g.setColor(new Color(0xFF9DB5));
            g.fill(new Ellipse2D.Double(mx - 1.6, -40.2, 3.2, 1.8));
        }
        else if(sad) {
            g.draw(new Arc2D.Double(mx - 2.6, -41.6, 5.2, 4, 20, 140, Arc2D.OPEN));
            g.setColor(new Color(110, 190, 255, 210));                   // a little tear
            double ty = -43 + (t * 12 % 9);
            g.fill(new Ellipse2D.Double(look + 7.2, ty, 2.6, 3.6));
        }
        else {
            g.draw(new Arc2D.Double(mx - 2.8, -43.2, 5.6, 4.2, 200, 140, Arc2D.OPEN));
        }
        g.setStroke(new BasicStroke(1f));

        // ---- fringe (under the hat brim)
        g.setColor(HAIR);
        double[][] bangs = { { -11, -54, 9 }, { -4.5, -55, 10 }, { 3, -55, 10 }, { 9.5, -54, 9 } };
        for(double[] b : bangs) {
            g.fill(new Ellipse2D.Double(b[0] - b[2] / 2 + look * 0.4, b[1] - 3, b[2], 8.5));
        }
        g.setColor(Theme.ROSE);                                         // a pink streak
        g.fill(new Ellipse2D.Double(1.5 + look * 0.4, -57, 3, 7.5));

        // ---- the pointy hat
        double bend = Math.sin(t * 2.0) * 1.6 + facing * 2;
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(-24, -59, 48, 8));                  // brim shadow on the face
        Path2D.Double cone = new Path2D.Double();
        cone.moveTo(-14, -60);
        cone.curveTo(-13, -74, -6, -84, 2, -90);
        cone.curveTo(6, -93, 11 + bend, -93, 15 + bend, -85);
        cone.curveTo(8 + bend * 0.5, -83, 11, -72, 14, -60);
        cone.closePath();
        g.setPaint(new GradientPaint(0, -94, HAT_TOP, 0, -58, HAT_BOTTOM));
        g.fill(cone);
        g.setColor(new Color(255, 255, 255, 45));
        g.fill(new Ellipse2D.Double(-9, -78, 3.5, 12));                 // soft highlight
        g.setPaint(new GradientPaint(0, -66, HAT_TOP, 0, -56, HAT_BOTTOM));
        g.fill(new Ellipse2D.Double(-25, -64.5, 50, 9));                // brim
        g.setColor(new Color(0x7B5BE0));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Arc2D.Double(-25, -64.5, 50, 9, 190, 160, Arc2D.OPEN));
        Path2D.Double band = new Path2D.Double();                       // rose band + gold buckle
        band.moveTo(-13.6, -64.5);
        band.quadTo(0, -61, 13.6, -64.5);
        band.lineTo(14, -60.5);
        band.quadTo(0, -57, -14, -60.5);
        band.closePath();
        g.setColor(Theme.ROSE_DARK);
        g.fill(band);
        g.setColor(GOLDY);
        g.fill(new RoundRectangle2D.Double(-3, -64.5, 6, 6, 2, 2));
        g.setColor(HAT_BOTTOM);
        g.fill(new RoundRectangle2D.Double(-1.5, -63, 3, 3, 1, 1));
        double tw = 0.75 + 0.25 * Math.sin(t * 4);
        Theme.drawStar(g, -3, -77, 3.6 * tw, GOLDY);                    // twinkling star on the hat
        Theme.drawSparkle(g, 9 + bend, -79, 2.2 * tw, Color.WHITE);

        g.dispose();
    }

    private static void drawArm(Graphics2D g, int side, double swing, double t, Mood mood)
    {
        double sx = side * 8.5, sy = -32.5;
        double ang;                                         // 0 = straight down, PI = straight up
        if(mood == Mood.HAPPY) {
            ang = Math.PI - 0.6 + Math.sin(t * 5.5) * 0.15;
        }
        else if(mood == Mood.WAVE && side > 0) {
            ang = Math.PI - 0.75 + Math.sin(t * 9) * 0.35;
        }
        else if(mood == Mood.SAD) {
            ang = 0.12;
        }
        else {
            ang = 0.24 + swing * 0.45 * side + Math.sin(t * 2.6) * 0.03;
        }
        double len = 12.5;
        double ex = sx + side * Math.sin(ang) * len;
        double ey = sy + Math.cos(ang) * len;
        g.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(DRESS_TOP);                                      // puffy sleeve
        g.draw(new Line2D.Double(sx, sy, sx + (ex - sx) * 0.4, sy + (ey - sy) * 0.4));
        g.setColor(SKIN);
        g.draw(new Line2D.Double(sx + (ex - sx) * 0.4, sy + (ey - sy) * 0.4, ex, ey));
        g.setStroke(new BasicStroke(1f));
        g.fill(new Ellipse2D.Double(ex - 2.9, ey - 2.9, 5.8, 5.8));
    }

    // ---------------------------------------------------------------- friends

    /** A shy little ghost who floats up and down. */
    public static void drawGhost(Graphics2D g0, double x, double feetY, double s, double t)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        g.translate(x, feetY);
        g.scale(s, s);
        double fl = Math.sin(t * 2.4) * 3.2;
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new Ellipse2D.Double(-10 + fl * 0.2, -2.5, 20 - Math.abs(fl), 5));
        g.translate(0, -10 + fl);
        Path2D.Double body = new Path2D.Double();
        body.moveTo(-12, 0);
        body.curveTo(-14, -26, -6, -34, 0, -34);
        body.curveTo(6, -34, 14, -26, 12, 0);
        double w = Math.sin(t * 5) * 1.5;
        body.quadTo(9, 5 + w, 6, 1);
        body.quadTo(3, -3 - w, 0, 1);
        body.quadTo(-3, 5 + w, -6, 1);
        body.quadTo(-9, -3 - w, -12, 0);
        body.closePath();
        g.setPaint(new GradientPaint(0, -34, new Color(0xFFFFFF), 0, 4, new Color(0xE4DAFB)));
        g.fill(body);
        g.setColor(new Color(0xCDBDF5));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(body);
        g.setColor(EYE);
        g.fill(new Ellipse2D.Double(-6.5, -21, 4, 5));
        g.fill(new Ellipse2D.Double(2.5, -21, 4, 5));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-5.8, -20.6, 1.6, 1.6));
        g.fill(new Ellipse2D.Double(3.2, -20.6, 1.6, 1.6));
        g.setColor(new Color(255, 130, 170, 120));
        g.fill(new Ellipse2D.Double(-10.5, -16.5, 5.5, 3.2));
        g.fill(new Ellipse2D.Double(5, -16.5, 5.5, 3.2));
        g.setColor(new Color(0xB5546F));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Double(-1.6, -15, 3.2, 3.6));
        g.dispose();
    }

    /** A sleepy black cat with glowing eyes and a wagging tail. */
    public static void drawCat(Graphics2D g0, double x, double feetY, double s, double t)
    {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.quality(g);
        g.translate(x, feetY);
        g.scale(s, s);
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(-13, -3, 26, 6));
        Color fur = new Color(0x2B2145), furLight = new Color(0x41345F);

        double wag = Math.sin(t * 2.4) * 5;                               // tail
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(8, -4);
        tail.curveTo(20, -3, 22 + wag, -14, 15 + wag, -24);
        g.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(fur);
        g.draw(tail);
        g.setStroke(new BasicStroke(1f));

        g.setPaint(new GradientPaint(0, -24, furLight, 0, 0, fur));        // body
        g.fill(new Ellipse2D.Double(-10, -22, 20, 21));
        g.setColor(new Color(0x5C4C80));                                    // paws
        g.fill(new Ellipse2D.Double(-8, -4, 6, 4));
        g.fill(new Ellipse2D.Double(2, -4, 6, 4));

        double hy = -29 + Math.sin(t * 2.0) * 0.6;                          // head
        for(int side = -1; side <= 1; side += 2) {                          // ears
            Path2D.Double ear = new Path2D.Double();
            ear.moveTo(side * 3, hy - 6);
            ear.lineTo(side * 8.5, hy - 17);
            ear.lineTo(side * 10.5, hy - 3);
            ear.closePath();
            g.setColor(fur);
            g.fill(ear);
            g.setColor(Theme.ROSE_DARK);
            Path2D.Double in = new Path2D.Double();
            in.moveTo(side * 5.2, hy - 6);
            in.lineTo(side * 8.3, hy - 13);
            in.lineTo(side * 9, hy - 5);
            in.closePath();
            g.fill(in);
        }
        g.setPaint(new GradientPaint(0f, (float) (hy - 10), furLight, 0f, (float) (hy + 9), fur));
        g.fill(new Ellipse2D.Double(-11, hy - 9, 22, 18));
        boolean blink = (t % 4.2) > 4.05;
        for(int side = -1; side <= 1; side += 2) {                          // glowing eyes
            double ex = side * 4.6;
            g.setColor(blink ? new Color(0xFFE07A) : new Color(0xFFE07A));
            g.fill(new Ellipse2D.Double(ex - 2.6, hy - 3, 5.2, blink ? 0.9 : 5.4));
            if(!blink) {
                g.setColor(EYE);
                g.fill(new Ellipse2D.Double(ex - 0.7, hy - 2.4, 1.4, 4.2));
            }
            g.setColor(new Color(255, 120, 170, 110));
            g.fill(new Ellipse2D.Double(side * 7.2 - 2.2, hy + 1.6, 4.4, 2.6));
        }
        g.setColor(Theme.ROSE_DARK);
        Path2D.Double nose = new Path2D.Double();
        nose.moveTo(-1.5, hy + 2.8);
        nose.lineTo(1.5, hy + 2.8);
        nose.lineTo(0, hy + 4.4);
        nose.closePath();
        g.fill(nose);
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(0.8f));
        for(int side = -1; side <= 1; side += 2) {                          // whiskers
            g.draw(new Line2D.Double(side * 6, hy + 4, side * 14, hy + 2.5));
            g.draw(new Line2D.Double(side * 6, hy + 5, side * 14, hy + 6.5));
        }
        g.setStroke(new BasicStroke(1f));
        // a tiny gold bell collar
        g.setColor(Theme.ROSE);
        g.fill(new RoundRectangle2D.Double(-7, hy + 8, 14, 2.6, 2, 2));
        g.setColor(GOLDY);
        g.fill(new Ellipse2D.Double(-1.7, hy + 9, 3.4, 3.4));
        g.dispose();
    }
}
