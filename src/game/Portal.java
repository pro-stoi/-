package game;

import java.util.ArrayList;
import java.util.List;

public class Portal {

    public static final int CELLS = 5;

    public static class Cell {
        public Figure figure;
        public double angle;
        public Cell(double a) { this.angle = a; }
    }

    private long id;
    private int fromNodeId;

    public int deltaTime;
    public int deltaWorld;

    private int costEnergy;
    public String label;
    public String customLabel = null;   // пользовательская подпись (под порталом)
    private String state;   // open | closed
    private String comboHash;

    public double angle;
    public double radius;
    public boolean anomalous = false;

    public enum Kind { TIME_FUTURE, TIME_PAST, PARALLEL }
    public Kind kind = Kind.TIME_FUTURE;
    public int targetBranch = -1;

    // пара «вход↔выход»: id связанного портала (в другом узле или в другой ветке)
    public long linkedPortalId = -1;

    // ячейки могут быть общими с парным порталом
    private List<Cell> cells = new ArrayList<>();

    public Portal(int deltaTime, int deltaWorld, int costEnergy) {
        this.deltaTime = deltaTime;
        this.deltaWorld = deltaWorld;
        this.costEnergy = costEnergy;
        this.label = "ΔT=" + deltaTime;
        this.state = "closed";
        for (int i = 0; i < CELLS; i++) {
            cells.add(new Cell(i * 2 * Math.PI / CELLS));
        }
    }

    // связать ячейки с другим порталом — теперь они физически одни
    public void shareCellsWith(Portal other) {
        this.cells = other.cells;
        this.refreshState();
    }

    public List<Cell> getCellsShared() { return cells; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getFromNodeId() { return fromNodeId; }
    public void setFromNodeId(int n) { this.fromNodeId = n; }
    public int getDeltaTime() { return deltaTime; }
    public void setDeltaTime(int dt) { this.deltaTime = dt; }
    public int getCostEnergy() { return costEnergy; }
    public void setCostEnergy(int c) { this.costEnergy = c; }
    public String getLabel() { return label; }
    public void setLabel(String l) { this.label = l; }
    public String getState() { return state; }
    public void setState(String s) { this.state = s; }
    public String getComboHash() { return comboHash; }
    public void setComboHash(String h) { this.comboHash = h; }
    public List<Cell> getCells() { return cells; }

    public int filledCells() {
        int c = 0;
        for (Cell cell : cells) if (cell.figure != null) c++;
        return c;
    }

    public void refreshState() {
        this.state = (filledCells() == CELLS) ? "open" : "closed";
    }

       @Override
    public String toString() {
        return "Портал[" + label + ", " + filledCells() + "/5, " + state + "]";
    }

    /**
     * Равномерно распределяет все порталы узла по кругу.
     * Если порталов N — каждый получает угол i * 2π/N.
     * Радиус одинаковый (R_PORTAL), если не задан особо.
     */
    public static void repositionAll(java.util.List<Portal> portals) {
        if (portals == null || portals.isEmpty()) return;
        int n = portals.size();
        double step = 2 * Math.PI / n;
        double start = -Math.PI / 2;   // первый — сверху

        for (int i = 0; i < n; i++) {
            Portal p = portals.get(i);
            p.angle = start + i * step;
            // радиус одинаковый, но чуть разный для аномалий можно оставить
            if (p.radius <= 0) p.radius = 1.30;
        }
    }
}
    
    
