package ui;

import game.Node;
import game.Figure;

import java.awt.*;
import java.util.List;

public class ForecastLayer {

    public static final int CELL = 64;
    public static final int GAP  = 14;

    public static int totalWidth() { return 5 * CELL + 4 * GAP; }

    public static void draw(Graphics2D g2, int width, Node node, List<Hit> hits) {
        int total = totalWidth();
        int x0 = (width - total) / 2;
        int y = 6;

        g2.setColor(Palette.TEXT_DIM);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        //g2.drawString("Прогноз появления (заряжено — не вернуть)", x0, y + CELL + 28);

               Figure.Shape[] shapes = Figure.Shape.values();
        visual.VisualSpec spec = visual.VisualRegistry.forBranch(
                node.getBranchId());
        for (int i = 0; i < 5; i++) {
            int x = x0 + i * (CELL + GAP);
            g2.setColor(Palette.PANEL);
            g2.fillRoundRect(x, y, CELL, CELL, 10, 10);
            g2.setColor(Palette.PANEL_EDGE);
            g2.drawRoundRect(x, y, CELL, CELL, 10, 10);

            // фигура прогноза — SVG текущей ветки
            boolean drew = visual.FigureRenderer.drawFigureSvg(
                    g2, shapes[i], x + CELL / 2, y + 30, 24, spec);
            if (!drew) {
                Shape shape = ShapePainter.shapeFor(shapes[i], x + CELL / 2, y + 30, 14);
                g2.setColor(Palette.TEXT);
                g2.fill(shape);
                g2.setColor(new Color(20, 24, 36, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(shape);
                g2.setStroke(new BasicStroke(1f));
            }

                    int pct = (int) Math.round(node.getForecast()[i] * 100);
            int charged = node.getForecastCharged()[i];

            // === проценты — ПОД ячейкой ===
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String ps = pct + "%";

            // подложка для читаемости
            int tw = fm.stringWidth(ps);
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(x + CELL / 2 - tw / 2 - 5, y + CELL + 4, tw + 10, 20, 6, 6);

            g2.setColor(new Color(250, 250, 250));
            g2.drawString(ps, x + CELL / 2 - tw / 2, y + CELL + 19);

            // === счётчик — ниже процентов ===
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            fm = g2.getFontMetrics();
            String cs = "×" + charged;
            int cw = fm.stringWidth(cs);

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(x + CELL / 2 - cw / 2 - 4, y + CELL + 26, cw + 8, 16, 5, 5);

            g2.setColor(new Color(255, 200, 140));
            g2.drawString(cs, x + CELL / 2 - cw / 2, y + CELL + 38);

            hits.add(new Hit(Hit.Kind.FORECAST, i, x + CELL / 2, y + CELL / 2, CELL / 2));
        }
    }
}