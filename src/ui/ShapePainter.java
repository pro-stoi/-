package ui;

import game.Figure;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class ShapePainter {

    public static Shape shapeFor(Figure.Shape s, int x, int y, int r) {
        switch (s) {
            case CIRCLE:     return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
            case SQUARE:     return new RoundRectangle2D.Double(
                                    x - r * 0.9, y - r * 0.9, r * 1.8, r * 1.8, r * 0.3, r * 0.3);
            case DIAMOND:    return diamond(x, y, r);
            case TRIANGLE:   return polygonShape(x, y, r, 3);
            case POLYHEDRON: return polygonShape(x, y, r, 5);
        }
        return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
    }

    public static Shape diamond(int cx, int cy, int r) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(cx, cy - r);
        p.lineTo(cx + r, cy);
        p.lineTo(cx, cy + r);
        p.lineTo(cx - r, cy);
        p.closePath();
        return p;
    }

    public static Shape polygonShape(int cx, int cy, int r, int n) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double a = -Math.PI / 2 + i * 2 * Math.PI / n;
            double px = cx + Math.cos(a) * r;
            double py = cy + Math.sin(a) * r;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

   public static void drawFigure(Graphics2D g2, Figure f, int x, int y, int size) {
        Color c = Color.WHITE;
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 55));
    g2.fill(new Ellipse2D.Double(x - size, y - size, size * 2, size * 2));

    g2.setColor(c);
    Shape shape = shapeFor(f.getShape(), x, y, size);
    g2.fill(shape);

    g2.setColor(new Color(20, 24, 36, 200));
    g2.setStroke(new BasicStroke(Math.max(1f, size * 0.12f)));
    g2.draw(shape);
    g2.setStroke(new BasicStroke(1f));
}

        public static void drawFigureTinted(Graphics2D g2, Figure f, int x, int y,
                                        int size, Color tint) {
        Color base = Color.WHITE;
        // смешиваем с оттенком ветки (30% tint)
    int r = (base.getRed()   * 7 + tint.getRed()   * 3) / 10;
    int g = (base.getGreen() * 7 + tint.getGreen() * 3) / 10;
    int b = (base.getBlue()  * 7 + tint.getBlue()  * 3) / 10;
    Color c = new Color(r, g, b);

        // свечение
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 55));
        g2.fill(new Ellipse2D.Double(x - size, y - size, size * 2, size * 2));

        // тело
        g2.setColor(c);
        Shape shape = shapeFor(f.getShape(), x, y, size);
        g2.fill(shape);

        // контур
        g2.setColor(new Color(20, 24, 36, 200));
        g2.setStroke(new BasicStroke(Math.max(1f, size * 0.12f)));
        g2.draw(shape);
        g2.setStroke(new BasicStroke(1f));

        // светящаяся точка в центре для «сияния»
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fill(new Ellipse2D.Double(x - size * 0.15, y - size * 0.15,
                                      size * 0.3, size * 0.3));
    }
}