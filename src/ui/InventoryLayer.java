package ui;

import game.Player;
import game.Figure;

import java.awt.*;
import java.util.List;

public class InventoryLayer {

    public static final int CELL = 64;
    public static final int GAP  = 12;
    public static final int Y_FROM_BOTTOM = 110;

    public static final int SLOTS = 5;
    public static final int TOTAL_CELLS = 6;   // 5 слотов + обменник

    // старая сигнатура — оставлена для совместимости, вызывает новую
    public static void draw(Graphics2D g2, int width, int height, Player player,
                            List<Hit> hits, int dragIndex, boolean isDragSource) {
        draw(g2, width, height, player, hits, dragIndex, isDragSource, false, null);
    }

    // новая сигнатура с обменником
    public static void draw(Graphics2D g2, int width, int height, Player player,
                            List<Hit> hits, int dragIndex, boolean isDragSource,
                            boolean exchangerActive, Figure.Shape exchangeShape) {

        int total = TOTAL_CELLS * CELL + (TOTAL_CELLS - 1) * GAP;
        int x0 = (width - total) / 2;
        int y = height - Y_FROM_BOTTOM;

        g2.setColor(Palette.TEXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Инвентарь (5 стопок) + обменник:", x0, y - 8);

        Player.Slot[] slots = player.getSlots();
              for (int i = 0; i < SLOTS; i++) {
            int x = x0 + i * (CELL + GAP);
            drawSlot(g2, x, y, slots[i], i, hits, dragIndex, isDragSource, player);
        }

                        int ex = x0 + SLOTS * (CELL + GAP);
        drawExchanger(g2, ex, y, exchangerActive, exchangeShape, hits, player);
    }

         private static void drawSlot(Graphics2D g2, int x, int y, Player.Slot s,
                                 int idx, List<Hit> hits,
                                 int dragIndex, boolean isDragSource,
                                 Player player) {
        g2.setColor(Palette.PANEL);
        g2.fillRoundRect(x, y, CELL, CELL, 10, 10);
        g2.setColor(Palette.PANEL_EDGE);
        g2.drawRoundRect(x, y, CELL, CELL, 10, 10);

        if (!s.isEmpty()) {
                      Figure f = new Figure(s.shape);
            // спек текущей ветки игрока
            visual.VisualSpec spec = visual.VisualRegistry.forBranch(
                    player.getCurrentBranch());
            boolean drew = visual.FigureRenderer.drawFigureSvg(
                    g2, s.shape, x + CELL / 2, y + CELL / 2, 22, spec);
            if (!drew) {
                ShapePainter.drawFigure(g2, f, x + CELL / 2, y + CELL / 2, 22);
            }

                    if (s.count > 1) {
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRoundRect(x + CELL - 26, y + CELL - 22, 24, 20, 6, 6);

                // полный слот — золотой, иначе — белый
                if (s.count >= Player.STACK_MAX) {
                    g2.setColor(new Color(255, 200, 90));
                } else {
                    g2.setColor(Color.WHITE);
                }

                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                String c = "×" + s.count;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(c, x + CELL - 14 - fm.stringWidth(c) / 2 + 2,
                              y + CELL - 7);
            }
        } else {
            g2.setColor(Palette.TEXT_DIM);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            String t = "пусто";
            g2.drawString(t, x + CELL / 2 - fm.stringWidth(t) / 2, y + CELL / 2 + 4);
        }

        hits.add(new Hit(Hit.Kind.SLOT, idx, x + CELL / 2, y + CELL / 2, CELL / 2));
    }

    private static void drawExchanger(Graphics2D g2, int x, int y,
                                      boolean active, Figure.Shape shape,
                                      List<Hit> hits, Player player) {
        g2.setColor(active ? new Color(40, 50, 80) : new Color(28, 30, 40));
        g2.fillRoundRect(x, y, CELL, CELL, 10, 10);
        g2.setColor(active ? new Color(255, 200, 90) : new Color(80, 90, 110));
        g2.setStroke(new BasicStroke(active ? 2f : 1f));
        g2.drawRoundRect(x, y, CELL, CELL, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        if (active && shape != null) {
            Figure next = new Figure(Figure.nextShape(shape));

            // === фигура превью — из спек ветки ===
            visual.VisualSpec exSpec = visual.VisualRegistry.forBranch(
                    player.getCurrentBranch());
            boolean drew = visual.FigureRenderer.drawFigureSvg(
                    g2, next.getShape(),
                    x + CELL / 2, y + CELL / 2 - 6, 18, exSpec);
            if (!drew) {
                ShapePainter.drawFigure(g2, next, x + CELL / 2, y + CELL / 2 - 6, 18);
            }

            // === подпись "5 → 1" — как было ===
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            String t = "5 → 1";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, x + CELL / 2 - fm.stringWidth(t) / 2, y + CELL - 6);
        } else {
            // === подпись "обмен"/"t±5" — как было ===
            g2.setColor(Palette.TEXT_DIM);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            String t = active ? "обмен" : "t±5";
            g2.drawString(t, x + CELL / 2 - fm.stringWidth(t) / 2, y + CELL / 2 + 4);
        }

        // === Hit — как было ===
        hits.add(new Hit(Hit.Kind.EXCHANGER, 0,
                         x + CELL / 2, y + CELL / 2, CELL / 2));
    }
}