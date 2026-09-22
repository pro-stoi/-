package ui;

import game.Figure;
import game.GameWorld;
import game.Node;
import game.Portal;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class WorldLayer {

       public static void draw(Graphics2D g2, int cx, int cy, int worldR, Node node,
                            GameWorld world, game.Player player,
                            List<Hit> hits, Figure dragging,
                            int dragIndex, boolean isDragField) {

        BranchSkin skin = BranchSkin.of(node.getBranchId());

        // ===== свечение вокруг мира =====
         // ===== мир: сначала пробуем SVG из спека =====
        visual.VisualSpec spec =
                visual.VisualRegistry.forBranch(node.getBranchId());

        boolean drewFromSvg = visual.WorldRenderer.drawSvgWorld(
                g2, cx, cy, worldR, spec, skin.glow, skin.worldEdge, node.getGlow());

        if (!drewFromSvg) {
            // fallback — старый круг
            for (int i = 6; i >= 1; i--) {
                int alpha = (int) (node.getGlow() * 40 / i);
                g2.setColor(new Color(skin.glow.getRed(), skin.glow.getGreen(),
                                      skin.glow.getBlue(), Math.max(4, alpha)));
                int rr = worldR + i * 8;
                g2.fill(new Ellipse2D.Double(cx - rr, cy - rr, rr * 2, rr * 2));
            }
            g2.setColor(new Color(30, 40, 70));
            g2.fill(new Ellipse2D.Double(cx - worldR, cy - worldR,
                                         worldR * 2, worldR * 2));
            g2.setColor(skin.worldEdge);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Double(cx - worldR, cy - worldR,
                                         worldR * 2, worldR * 2));
            g2.setStroke(new BasicStroke(1f));
        }

        // внутренняя заливка (fill) — всегда отдельно, не зависит от SVG
        double fillFrac = node.getFill() / 100.0;
        int fr = (int) (worldR * 0.55 * fillFrac);
        if (fr > 0) {
            g2.setColor(new Color(80, 220, 180, 60));
            g2.fill(new Ellipse2D.Double(cx - fr, cy - fr, fr * 2, fr * 2));
        }

        // ===== якорь времени (t=+5 и т.п.) =====
        g2.setColor(Palette.TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        String t = (node.getTimeAnchor() >= 0 ? "+" : "") + node.getTimeAnchor();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, cx - fm.stringWidth(t) / 2, cy + 8);

        // ===== предметы — из пула, фильтр по (branch, t) =====
             List<Figure> items = world.visibleOnField(node.getBranchId(), node.getTimeAnchor());
        visual.VisualSpec fSpec = visual.VisualRegistry.forBranch(node.getBranchId());
        for (int i = 0; i < items.size(); i++) {
            Figure f = items.get(i);
            int x = cx + (int) (Math.cos(f.getAngle()) * f.getRadius() * worldR * 0.85);
            int y = cy + (int) (Math.sin(f.getAngle()) * f.getRadius() * worldR * 0.85);
            if (dragging != null && isDragField && i == dragIndex) continue;

            boolean drew = visual.FigureRenderer.drawFigureSvg(
                    g2, f.getShape(), x, y, 18, fSpec);
            if (!drew) {
                ShapePainter.drawFigureTinted(g2, f, x, y, 18, skin.shapeTint);
            }
            hits.add(new Hit(Hit.Kind.ITEM, i, x, y, 24));
        }

        // ===== порталы =====
        for (int pi = 0; pi < node.getPortals().size(); pi++) {
            Portal p = node.getPortals().get(pi);
            int px = cx + (int) (Math.cos(p.angle) * p.radius * worldR);
            int py = cy + (int) (Math.sin(p.angle) * p.radius * worldR);

            Color c;
            if (p.anomalous) c = skin.anomaly;
            else if (p.kind == Portal.Kind.PARALLEL) c = skin.parallel;
            else if (p.kind == Portal.Kind.TIME_PAST) c = skin.past;
            else c = skin.future;

                    // ===== портал: пробуем SVG из спека ветки =====
                   // ===== портал: пробуем SVG из спека ветки =====
            boolean open = "open".equals(p.getState());
            visual.VisualSpec pSpec =
                    visual.VisualRegistry.forBranch(node.getBranchId());

            boolean drewPortalFromSvg = visual.PortalRenderer.drawPortalSvg(
                    g2, px, py, p, pSpec);

            if (!drewPortalFromSvg) {
                // fallback — старый хардкод
                if (open) {
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 70));
                    g2.fill(new Ellipse2D.Double(px - 34, py - 34, 68, 68));
                }
                g2.setColor(open ? c : new Color(80, 90, 120));
                g2.setStroke(new BasicStroke(open ? 2.5f : 1.5f));
                g2.draw(new Ellipse2D.Double(px - 28, py - 28, 56, 56));
                g2.setStroke(new BasicStroke(1f));
            
            }

                       g2.setColor(open ? Color.WHITE : new Color(160, 170, 200));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));

            String lbl;

            // back-портал (⇦ ветка N) — всегда видим
            if (p.label != null && p.label.startsWith("⇦")) {
                lbl = p.label;
            }
            // если игрок не знает, куда ведёт портал — показываем "?"
            else if (player == null || !player.isPortalKnown(p)) {
                lbl = "?";
            }
            else if (p.kind == Portal.Kind.PARALLEL) {
                lbl = "⇄" + p.targetBranch;
            } else {
                String sign = (p.kind == Portal.Kind.TIME_FUTURE ? "+" : "-");
                String q = p.anomalous ? "?" : "";
                lbl = sign + Math.abs(p.deltaTime) + q;
            }

            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(lbl, px - fm2.stringWidth(lbl) / 2, py + 4);
            // подпись под порталом (снаружи, дальше от ячеек)
            if (p.customLabel != null && !p.customLabel.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                FontMetrics fmc = g2.getFontMetrics();
                int lw = fmc.stringWidth(p.customLabel);

                double dx = px - cx;
                double dy = py - cy;
                double dist = Math.hypot(dx, dy);
                if (dist < 1) dist = 1;
                double nx = dx / dist;
                double ny = dy / dist;

                int off = 80;
                int lx = px + (int)(nx * off) - lw / 2;
                int ly = py + (int)(ny * off) + 4;

                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRoundRect(lx - 6, ly - 13, lw + 12, 18, 8, 8);

                Color labelColor = new Color(255, 230, 160);
                if (p.customLabel.startsWith("⚠")) labelColor = new Color(255, 120, 120);
                else if (p.customLabel.startsWith("¤")) labelColor = new Color(255, 200, 90);
                else if (p.customLabel.startsWith("←")) labelColor = new Color(120, 220, 160);
                else if (p.customLabel.startsWith("→")) labelColor = new Color(120, 180, 255);

                g2.setColor(labelColor);
                g2.drawString(p.customLabel, lx, ly);
            }

            int cr = 54;
            for (int ci = 0; ci < Portal.CELLS; ci++) {
                Portal.Cell cell = p.getCells().get(ci);
                double a = cell.angle - Math.PI / 2;
                int ccx = px + (int) (Math.cos(a) * cr);
                int ccy = py + (int) (Math.sin(a) * cr);

                boolean isDragSource = dragging != null && dragIndex == pi * 100 + ci
                        && !isDragField;

                            // ===== ячейка: пробуем SVG из спека =====
                boolean drewCellFromSvg = visual.CellRenderer.drawCellSvg(
                        g2, ccx, ccy, cell, pSpec);

                if (!drewCellFromSvg) {
                    // fallback — старый хардкод
                    g2.setColor(new Color(40, 50, 80));
                    g2.fill(new Ellipse2D.Double(ccx - 13, ccy - 13, 26, 26));
                    g2.setColor(open ? c : new Color(90, 100, 130));
                    g2.draw(new Ellipse2D.Double(ccx - 13, ccy - 13, 26, 26));
                }

                // фигура внутри ячейки — рисуем как раньше (этап 5 — тоже в SVG)
                               if (cell.figure != null && !isDragSource) {
                    boolean drewF = visual.FigureRenderer.drawFigureSvg(
                            g2, cell.figure.getShape(), ccx, ccy, 10, pSpec);
                    if (!drewF) {
                        ShapePainter.drawFigure(g2, cell.figure, ccx, ccy, 10);
                    }
                }
                hits.add(new Hit(Hit.Kind.PORTAL_CELL, pi * 100 + ci, ccx, ccy, 15));
            }
            hits.add(new Hit(Hit.Kind.PORTAL, pi, px, py, 32));
        }

        // ===== обменник =====
        if (node.hasExchanger()) {
            int ex = cx + (int) (Math.cos(node.getExchangerAngle())
                                 * node.getExchangerRadius() * worldR);
            int ey = cy + (int) (Math.sin(node.getExchangerAngle())
                                 * node.getExchangerRadius() * worldR);

            // внешнее свечение
            g2.setColor(new Color(255, 200, 90, 60));
            g2.fill(new Ellipse2D.Double(ex - 32, ey - 32, 64, 64));

            // кольцо
            g2.setColor(new Color(255, 200, 90));
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(new Ellipse2D.Double(ex - 24, ey - 24, 48, 48));
            g2.setStroke(new BasicStroke(1f));

            // внутренняя тёмная подложка
            g2.setColor(new Color(30, 26, 14));
            g2.fill(new Ellipse2D.Double(ex - 18, ey - 18, 36, 36));

            // символ курса
            g2.setColor(new Color(255, 230, 160));
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            String sym = "¤";
            FontMetrics fm3 = g2.getFontMetrics();
            g2.drawString(sym, ex - fm3.stringWidth(sym) / 2, ey + 6);

            hits.add(new Hit(Hit.Kind.EXCHANGER, 0, ex, ey, 28));
        }
    }
}