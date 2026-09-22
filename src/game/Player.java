package game;

import java.util.HashSet;
import java.util.Set;

public class Player {
    public static final int ENERGY_MAX = 20;
    public static final int STACK_MAX = 25;

    private int pastEnergy = ENERGY_MAX;
    private int futureEnergy = ENERGY_MAX;
    private int globalTime = 0;
    private int currentBranch = 0;
    private Node currentNode;

    private int gameYear = 0;

    private final boolean[] unlockedShapes = new boolean[Figure.Shape.values().length];

    // === известные паттерны порталов ===
    // "past:N", "future:N", "parallel:N", "anomaly:N"
    private final Set<String> knownPortalPatterns = new HashSet<>();

    // ---- слоты как стопки ----
    public static class Slot {
        public Figure.Shape shape;
        public int count;

        public boolean isEmpty() { return count <= 0; }
        public void clear() { shape = null; count = 0; }

        public boolean matches(Figure f) {
            return shape == f.getShape();
        }
    }

    private final Slot[] slots = new Slot[5];

    public Player() {
        for (int i = 0; i < 5; i++) slots[i] = new Slot();
        unlockedShapes[Figure.Shape.CIRCLE.ordinal()] = true;

        // ±1 всегда известны
        knownPortalPatterns.add("past:1");
        knownPortalPatterns.add("future:1");
    }

    // ---- работа с инвентарём ----
    public Slot[] getSlots() { return slots; }

    public boolean addFigure(Figure f) {
        if (f == null) return false;

        for (Slot s : slots) {
            if (!s.isEmpty() && s.matches(f) && s.count < STACK_MAX) {
                s.count++;
                return true;
            }
        }
        for (Slot s : slots) {
            if (s.isEmpty()) {
                s.shape = f.getShape();
                s.count = 1;
                return true;
            }
        }
        return false;
    }

    public Figure takeOneFromSlot(int slotIdx) {
        if (slotIdx < 0 || slotIdx >= 5) return null;
        Slot s = slots[slotIdx];
        if (s.isEmpty()) return null;

        Figure f = new Figure(s.shape);
        s.count--;
        if (s.count <= 0) s.clear();
        return f;
    }

    public java.util.List<Figure> takeManyFromSlot(int slotIdx, int n) {
        java.util.List<Figure> out = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            Figure f = takeOneFromSlot(slotIdx);
            if (f == null) break;
            out.add(f);
        }
        return out;
    }

    public int countInSlot(int slotIdx) {
        if (slotIdx < 0 || slotIdx >= 5) return 0;
        return slots[slotIdx].count;
    }

    // ---- энергия ----
    public int getPastEnergy() { return pastEnergy; }
    public int getFutureEnergy() { return futureEnergy; }
    public void spendPast(int v)   { pastEnergy   = Math.max(0, pastEnergy - v); }
    public void spendFuture(int v) { futureEnergy = Math.max(0, futureEnergy - v); }
    public void addPast(int v)     { pastEnergy   = Math.min(ENERGY_MAX, pastEnergy + v); }
    public void addFuture(int v)   { futureEnergy = Math.min(ENERGY_MAX, futureEnergy + v); }

    public int getGlobalTime() { return globalTime; }
    public void addGlobalTime(int dt) { this.globalTime += dt; }

    public int getCurrentBranch() { return currentBranch; }
    public void setCurrentBranch(int b) { this.currentBranch = b; }

    public Node getCurrentNode() { return currentNode; }
    public void setCurrentNode(Node n) { this.currentNode = n; }

    public int getGameYear() { return gameYear; }
    public void incrementYear() { gameYear++; }

    public boolean isShapeUnlocked(Figure.Shape s) {
        return unlockedShapes[s.ordinal()];
    }
    public void unlockShape(Figure.Shape s) {
        unlockedShapes[s.ordinal()] = true;
    }

    // =========================================================
    //  ПОРТАЛЫ — ЗНАНИЕ
    // =========================================================

    /** Паттерн портала для запоминания. */
     private static String portalPattern(Portal p) {
        if (p.anomalous) return "anomaly:" + p.targetBranch;
        if (p.kind == Portal.Kind.PARALLEL) return "parallel:" + p.targetBranch;
        if (p.kind == Portal.Kind.TIME_PAST) return "past:" + Math.abs(p.deltaTime);
        return "future:" + Math.abs(p.deltaTime);
    }

    /** Знает ли игрок, куда ведёт этот портал. */
    public boolean isPortalKnown(Portal p) {
        if (p == null) return false;
        // аномалия всегда "?"
        if (p.anomalous) return false;
        return knownPortalPatterns.contains(portalPattern(p));
    }

    /** Пометить портал как известный (после прыжка). */
    public void markPortalKnown(Portal p) {
        if (p == null) return;
        if (p.anomalous) return;   // аномалия не запоминается
        knownPortalPatterns.add(portalPattern(p));
    }

    // =========================================================
    //  ОБМЕН
    // =========================================================

    public boolean exchange(Figure.Shape shape, boolean forward) {
        int total = 0;
        for (Slot s : slots) {
            if (!s.isEmpty() && s.shape == shape) {
                total += s.count;
            }
        }
        if (total < 5) return false;

        int toRemove = 5;
        for (Slot s : slots) {
            if (toRemove == 0) break;
            if (s.isEmpty() || s.shape != shape) continue;
            int take = Math.min(s.count, toRemove);
            s.count -= take;
            toRemove -= take;
            if (s.count <= 0) s.clear();
        }

        Figure.Shape next = forward ? Figure.nextShape(shape) : Figure.prevShape(shape);
        Figure result = new Figure(next);
        boolean placed = addFigure(result);
        if (!placed) {
            for (int i = 0; i < 5; i++) {
                addFigure(new Figure(shape));
            }
            return false;
        }
        return true;
    }

    public boolean exchange(Figure.Shape shape) {
        return exchange(shape, true);
    }
}