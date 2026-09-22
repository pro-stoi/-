package ui;

import game.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class WorldPanel extends JPanel {

    private final GameWorld world;
    private final Player player;

    private int cx, cy, worldR;
    private double zoom = 1.0;
    private final List<Hit> hits = new ArrayList<>();
    private String status = "ЛКМ — тащить, ПКМ — быстро взять/прыгнуть, 2×ЛКМ — подписать портал";

    private Figure dragging = null;
    private int dragX, dragY, dragIndex = -1;
    private DragSource dragSource = DragSource.NONE;

    private final Timer yearTimer;
    private static final int YEAR_INTERVAL_MS = 20_000;
    private int secondsToNextYear = YEAR_INTERVAL_MS / 1000;

    private enum DragSource { NONE, FIELD, SLOT, PORTAL_CELL }

    public WorldPanel(GameWorld world, Player player) {
        this.world = world;
        this.player = player;
        setBackground(Palette.BG);
        setPreferredSize(new Dimension(980, 820));

 addMouseListener(new MouseAdapter() {
    @Override public void mousePressed(MouseEvent e) {
        if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
            onRightClick(e.getX(), e.getY());
        } else if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
            // СНАЧАЛА проверяем "мгновенные" клики (портал, зум, +1 год).
            // Если попали — НЕ начинаем drag.
            if (onInstantClick(e.getX(), e.getY())) return;
            // иначе — начинаем drag (взять фигуру и т.п.)
            onPress(e.getX(), e.getY());
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
            onRelease(e.getX(), e.getY());
        }
    }
});
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                dragX = e.getX(); dragY = e.getY(); repaint();
            }
        });

        // авто-ход времени: +1 год каждые 20 секунд
        yearTimer = new Timer(YEAR_INTERVAL_MS, e -> {
            Node n = player.getCurrentNode();
            if (n != null) {
                world.advanceYear(player, +1);
                status = "прошёл год → " + player.getGameYear();
            }
            secondsToNextYear = YEAR_INTERVAL_MS / 1000;
            repaint();
        });
        yearTimer.start();

        // отсчёт для HUD
        Timer countdown = new Timer(1000, e -> {
            if (secondsToNextYear > 0) secondsToNextYear--;
            repaint();
        });
        countdown.start();
        // анимация мира — 30 FPS
Timer anim = new Timer(33, e -> repaint());
anim.start();
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Node node = player.getCurrentNode();
        if (node == null) return;

        BranchSkin skin = BranchSkin.of(node.getBranchId());
        setBackground(skin.bg);

        hits.clear();

        cx = getWidth() / 2;
        cy = getHeight() / 2 + 30;
        worldR = (int) ((120 + node.getRadius() * 20) * zoom);

              ForecastLayer.draw(g2, getWidth(), node, hits);
        WorldLayer.draw(g2, cx, cy, worldR, node, world, player, hits,
                dragging, dragIndex, dragSource == DragSource.FIELD);

        // обменник: активен, если |t| >= 5
      InventoryLayer.draw(g2, getWidth(), getHeight(), player, hits,
        dragIndex, dragSource == DragSource.SLOT);
        HudLayer.draw(g2, node, player, secondsToNextYear); 
        drawZoom(g2);
        drawStatus(g2);

                if (dragging != null) {
            visual.VisualSpec dragSpec =
                    visual.VisualRegistry.forBranch(player.getCurrentBranch());
            boolean drew = visual.FigureRenderer.drawFigureSvg(
                    g2, dragging.getShape(), dragX, dragY, 22, dragSpec);
            if (!drew) {
                ShapePainter.drawFigure(g2, dragging, dragX, dragY, 22);
            }
        }
    }

   private void drawZoom(Graphics2D g2) {
    int bx = getWidth() - 70, by = 16, bs = 28;

    // кнопки зума
    drawBtn(g2, bx, by, bs, "+");
    drawBtn(g2, bx, by + bs + 8, bs, "−");

    // кнопка "+1 год"
    int wy = by + (bs + 8) * 2;
    int ww = 92;
    int wx = bx + bs - ww;

    drawWideBtn(g2, wx, wy, ww, bs, "+1 год");

    // ===== СЧЁТЧИК ГОДА ПОД КНОПКОЙ =====
    g2.setFont(new Font("SansSerif", Font.BOLD, 13));
    FontMetrics fm = g2.getFontMetrics();
    String s = "год " + player.getGameYear();

    // крупно, ярко — чтобы сразу видеть изменение
    g2.setColor(new Color(255, 230, 160));
    g2.drawString(s, wx + ww / 2 - fm.stringWidth(s) / 2, wy + bs + 16);

    hits.add(new Hit(Hit.Kind.ZOOM_IN, 0, bx + bs / 2, by + bs / 2, bs / 2));
    hits.add(new Hit(Hit.Kind.ZOOM_OUT, 0, bx + bs / 2, by + bs + 8 + bs / 2, bs / 2));
    hits.add(new Hit(Hit.Kind.NEXT_YEAR, 0, wx + ww / 2, wy + bs / 2, bs / 2));
    
    
}

    private void drawWideBtn(Graphics2D g2, int x, int y, int w, int h, String t) {
        g2.setColor(new Color(30, 36, 56));
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(Palette.PANEL_EDGE);
        g2.drawRoundRect(x, y, w, h, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, x + w / 2 - fm.stringWidth(t) / 2, y + h / 2 + 5);
    }

    private void drawBtn(Graphics2D g2, int x, int y, int s, String t) {
        g2.setColor(new Color(30, 36, 56));
        g2.fillRoundRect(x, y, s, s, 8, 8);
        g2.setColor(Palette.PANEL_EDGE);
        g2.drawRoundRect(x, y, s, s, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, x + s / 2 - fm.stringWidth(t) / 2, y + s / 2 + 6);
    }

   private void drawStatus(Graphics2D g2) {
    g2.setColor(Palette.TEXT);
    g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
    g2.drawString(status, 16, getHeight() - 16);

    g2.setColor(Palette.TEXT_DIM);
    g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
    g2.drawString("ПКМ: взять · прыгнуть · подписать · обмен",
                  16, getHeight() - 2);
}

    // ---------- мышь ----------
    private void onPress(int mx, int my) {
        Hit h = findHit(mx, my);
        if (h == null) return;
        Node node = player.getCurrentNode();
        dragX = mx; dragY = my;

        switch (h.kind) {
            case ITEM: {
                List<Figure> visible = world.visibleOnField(
                        node.getBranchId(), node.getTimeAnchor());
                if (h.index < 0 || h.index >= visible.size()) return;
                dragging = visible.get(h.index);
                dragSource = DragSource.FIELD;
                dragIndex = h.index;
                dragging.setLocation(Figure.Location.INVENTORY);
                status = "взял с поля";
                repaint();
                break;
            }
            case SLOT: {
                Figure f = player.takeOneFromSlot(h.index);
                if (f != null) {
                    dragging = f;
                    dragSource = DragSource.SLOT;
                    dragIndex = h.index;
                    status = "взял из слота " + h.index;
                } else {
                    status = "слот пуст";
                }
                repaint();
                break;
            }
            case PORTAL_CELL: {
                int pi = h.index / 100, ci = h.index % 100;
                Portal p = node.getPortals().get(pi);
                Figure f = p.getCells().get(ci).figure;
                if (f != null) {
                    if (p.filledCells() == Portal.CELLS) {
                        List<Figure> combo = new ArrayList<>();
                        for (Portal.Cell c : p.getCells()) combo.add(c.figure);
                        world.unregisterCombo(node.getBranchId(), combo);
                    }
                    dragging = f;
                    dragSource = DragSource.PORTAL_CELL;
                    dragIndex = h.index;
                    p.getCells().get(ci).figure = null;
                    p.refreshState();
                    status = "взял из портала";
                }
                repaint();
                break;
            }
        }
    }

    private void onRelease(int mx, int my) {
        if (dragging == null) return;
        Hit h = findHit(mx, my);
        Node node = player.getCurrentNode();

        if (h == null) { cancelDrag(); return; }

        switch (h.kind) {
            case SLOT: {
                boolean ok = player.addFigure(dragging);
                status = ok ? "положил в инвентарь" : "нет места — вернул";
                if (!ok) returnToSource(node);
                break;
            }
            case ITEM: {
                double a = Math.atan2(my - cy, mx - cx);
                double r = Math.min(0.85, Math.hypot(mx - cx, my - cy) / worldR);
                world.dropOnField(dragging, node.getBranchId(), node.getTimeAnchor(), a, r);
                status = "выбросил на поле";
                break;
            }
            case PORTAL_CELL: {
                int pi = h.index / 100, ci = h.index % 100;
                Portal p = node.getPortals().get(pi);
                if (p.getCells().get(ci).figure != null) { cancelDrag(); return; }

                p.getCells().get(ci).figure = dragging;

                if (p.filledCells() == Portal.CELLS) {
                    List<Figure> combo = new ArrayList<>();
                    for (Portal.Cell c : p.getCells()) combo.add(c.figure);

                                   if (!world.isComboAvailable(node.getBranchId(), combo)) {
                        p.getCells().get(ci).figure = null;
                        p.refreshState();
                        returnToSource(node);
                        dragging = null;
                        dragSource = DragSource.NONE;
                        dragIndex = -1;
                        status = "такая комбинация уже собрана в этой ветке";
                        repaint();
                        return;
                    }
                    world.registerCombo(node.getBranchId(), combo);
                }

                dragging.setLocation(Figure.Location.PORTAL_CELL);
                p.refreshState();
                status = "положил в портал (" + p.filledCells() + "/5)";
                if (p.filledCells() == Portal.CELLS && "closed".equals(p.getState())) {
                    PortalDialog.ask(this, p, world, player, () -> {
                        status = "портал настроен: " + p.getLabel();
                        repaint();
                    });
                }
                break;
            }
            case FORECAST: {
                int shapeIdx = h.index;
                if (dragging.getShape().ordinal() != shapeIdx) {
                    status = "форма не подходит";
                    cancelDrag();
                    return;
                }
                world.chargeForecast(node, shapeIdx, dragging);
                // фигура израсходована
                if (dragSource == DragSource.FIELD) {
                    world.pickUp(dragging);
                }
                status = "зарядил прогноз " + dragging.getShape();
                break;
            }
 case EXCHANGER: {
    if (!node.hasExchanger()) {
        status = "здесь нет обменника";
        returnToSource(node);
        break;
    }
    status = "кликни по обменнику, чтобы открыть диалог";
    returnToSource(node);
    break;
}
            default:
                cancelDrag();
                return;
        }
        dragging = null;
        dragSource = DragSource.NONE;
        dragIndex = -1;
        repaint();
    }

    private void returnToSource(Node node) {
        if (dragging == null) return;
        switch (dragSource) {
            case FIELD:
                world.dropOnField(dragging, node.getBranchId(), node.getTimeAnchor(),
                        dragging.getAngle(), dragging.getRadius());
                break;
            case SLOT:
                player.addFigure(dragging);
                break;
            case PORTAL_CELL: {
                int pi = dragIndex / 100, ci = dragIndex % 100;
                Portal p = node.getPortals().get(pi);
                p.getCells().get(ci).figure = dragging;
                dragging.setLocation(Figure.Location.PORTAL_CELL);
                p.refreshState();
                break;
            }
            default:
                break;
        }
    }

    private void cancelDrag() {
        returnToSource(player.getCurrentNode());
        dragging = null;
        dragSource = DragSource.NONE;
        dragIndex = -1;
        status = "отмена";
        repaint();
    }

    // ---------- мгновенные клики ЛКМ (без drag) ----------
// Возвращает true, если клик обработан и drag начинать НЕ надо.
private boolean onInstantClick(int mx, int my) {
    Hit h = findHit(mx, my);
    if (h == null) return false;

    Node node = player.getCurrentNode();

    switch (h.kind) {
        case PORTAL: {
            Portal p = node.getPortals().get(h.index);
            if (!"open".equals(p.getState())) {
                status = "портал закрыт (" + p.filledCells() + "/5)";
                repaint();
                return true;
            }
            try {
                world.jump(player, p);
                status = "прыжок → " + player.getCurrentNode().getName()
                        + " (ветка " + player.getCurrentBranch() + ")";
            } catch (Exception ex) {
                status = "прыжок невозможен: " + ex.getMessage();
            }
            repaint();
            return true;
        }
        case ZOOM_IN: {
            zoom = Math.min(2.0, zoom + 0.15);
            repaint();
            return true;
        }
        case ZOOM_OUT: {
            zoom = Math.max(0.5, zoom - 0.15);
            repaint();
            return true;
        }
        case NEXT_YEAR: {
            world.advanceYear(player, +1);
            secondsToNextYear = YEAR_INTERVAL_MS / 1000;
            status = "год " + player.getGameYear();
            repaint();
            return true;
        }
        default:
            return false;   // не мгновенный — пусть работает drag
    }
}
    
    
 

    private Hit findHit(int mx, int my) {
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            int dx = mx - h.x, dy = my - h.y;
            if (dx * dx + dy * dy <= h.r * h.r) return h;
        }
        return null;
    }
    
    // ---------- переименование портала ----------
private void renamePortal(int portalIndex) {
    Node node = player.getCurrentNode();
    if (portalIndex < 0 || portalIndex >= node.getPortals().size()) return;
    Portal p = node.getPortals().get(portalIndex);

    String current = (p.customLabel == null) ? "" : p.customLabel;
    String input = (String) JOptionPane.showInputDialog(
            this,
            "Подпись под порталом (пусто — убрать):\n"
                + "Системная метка: " + p.getLabel(),
            "Переименовать портал",
            JOptionPane.PLAIN_MESSAGE,
            null, null, current);

    if (input == null) return;   // отмена

    input = input.trim();
    if (input.isEmpty()) {
        p.customLabel = null;
        status = "подпись убрана";
    } else {
        if (input.length() > 20) input = input.substring(0, 20);
        p.customLabel = input;
        status = "подпись: «" + input + "»";
    }
    repaint();
}
    
    
    private void openExchangerDialog(Node node) {
    if (!node.hasExchanger()) {
        status = "здесь нет обменника";
        repaint();
        return;
    }
    if (node.getTimeAnchor() == 0) {
        status = "обменник не работает в нулевой зоне";
        repaint();
        return;
    }
    boolean forward = node.getTimeAnchor() > 0;

        Player.Slot[] slots = player.getSlots();
    java.util.List<String> opts = new ArrayList<>();
    java.util.List<Figure.Shape> shapes = new ArrayList<>();

      for (Player.Slot s : slots) {
        if (!s.isEmpty() && s.count >= 5) {
            Figure.Shape target = forward
                    ? Figure.nextShape(s.shape)
                    : Figure.prevShape(s.shape);
            opts.add(s.shape + " × " + s.count + " → " + target);
            shapes.add(s.shape);
        }
    }
    if (opts.isEmpty()) {
        status = "нужно 5 одинаковых фигур в слоте";
        repaint();
        return;
    }
    Object[] arr = opts.toArray();
    int choice = JOptionPane.showOptionDialog(this,
            forward ? "Обмен вперёд (t > 0)" : "Обмен назад (t < 0)",
            "Обменник",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, arr, arr[0]);
    if (choice < 0 || choice >= shapes.size()) {
        status = "обмен отменён";
        repaint();
        return;
    }
        boolean ok = player.exchange(shapes.get(choice), forward);
    status = ok
            ? "обменял 5 × " + shapes.get(choice) + " → "
              + (forward ? Figure.nextShape(shapes.get(choice))
                         : Figure.prevShape(shapes.get(choice)))
            : "не удалось обменять";
    repaint();
}
    
 // ---------- ПКМ: быстрые действия ----------
private void onRightClick(int mx, int my) {
    Hit h = findHit(mx, my);
    if (h == null) return;
    Node node = player.getCurrentNode();

    switch (h.kind) {
        case ITEM: {
            java.util.List<Figure> visible = world.visibleOnField(
                    node.getBranchId(), node.getTimeAnchor());
            if (h.index < 0 || h.index >= visible.size()) return;
            Figure f = visible.get(h.index);
            boolean ok = player.addFigure(f);
            if (ok) {
                world.pickUp(f);   // убираем с поля навсегда
                status = "взял в инвентарь: " + f.getShape();
            } else {
                status = "инвентарь полон";
            }
            break;
        }
        case PORTAL_CELL: {
            int pi = h.index / 100, ci = h.index % 100;
            Portal p = node.getPortals().get(pi);
            Figure f = p.getCells().get(ci).figure;
            if (f == null) { status = "ячейка пуста"; break; }

            // снимаем комбо, если портал был полон
                             if (p.filledCells() == Portal.CELLS) {
                        List<Figure> combo = new ArrayList<>();
                        for (Portal.Cell c : p.getCells()) combo.add(c.figure);
                        world.unregisterCombo(node.getBranchId(), combo);
                    }

            boolean ok = player.addFigure(f);
            if (ok) {
                p.getCells().get(ci).figure = null;
                p.refreshState();
                status = "взял из портала: " + f.getShape();
            } else {
                status = "инвентарь полон";
            }
            break;
        }
      case PORTAL: {
    // ПКМ по порталу = переименование
    renamePortal(h.index);
    break;
}
        case EXCHANGER: {
            openExchangerDialog(node);
            break;
        }
        case FORECAST: {
            // ПКМ по прогнозу — зарядить форму, если она есть в инвентаре
            int shapeIdx = h.index;
            Figure.Shape need = Figure.Shape.values()[shapeIdx];

            // ищем в слотах
            Player.Slot[] slots = player.getSlots();
            for (int si = 0; si < slots.length; si++) {
                Player.Slot s = slots[si];
                if (!s.isEmpty() && s.shape == need) {
                    Figure f = player.takeOneFromSlot(si);
                    if (f != null) {
                        world.chargeForecast(node, shapeIdx, f);
                        status = "зарядил прогноз " + need + " (из слота)";
                    }
                    break;
                }
            }
            break;
        }
        default:
            break;
    }
    repaint();
}   
}