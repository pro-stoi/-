package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GameWorld {

    public static boolean DEBUG_INFINITE_ENERGY = true;

    public static final int EXCHANGER_FIRST_T = 5;    // первый обменник в ветке
    public static final int EXCHANGER_MIN_GAP = 5;    // минимум лет между обменниками
    public static final int EXCHANGER_MAX_GAP = 20;   // максимум лет между обменниками

    // t-координаты обменников по веткам (для правила «шаг 5…20»)
    private final Map<Integer, java.util.TreeSet<Integer>> exchangerTs = new HashMap<>();

    private java.util.TreeSet<Integer> exchangerTimes(int branchId) {
        return exchangerTs.computeIfAbsent(branchId, k -> new java.util.TreeSet<>());
    }

    private final long seed;
    private final Map<Integer, Map<Integer, Node>> allBranches = new HashMap<>();
     private final Map<Integer, Set<String>> usedCombosByBranch = new HashMap<>();
    private Node currentNode;

    private long nextNodeId = 1;
    private long nextPortalId = 1;

    private final Map<Integer, java.util.TreeSet<Integer>> exchangerPos = new HashMap<>();
    private final Map<Integer, java.util.TreeSet<Integer>> exchangerNeg = new HashMap<>();

    public static final double R_PORTAL = 1.30;
    public static final int FREE_RANGE = 5;         // ±5 вокруг t=0 — «нулевая зона»

    public GameWorld(long seed) { this.seed = seed; }

    public long getSeed() { return seed; }

    public Node getCurrentNode() { return currentNode; }
    public void setCurrentNode(Node n) { this.currentNode = n; }

    // ---------- ветки ----------
    public Map<Integer, Node> branch(int id) {
        return allBranches.computeIfAbsent(id, k -> new HashMap<>());
    }
    public Node getNode(int branchId, int t) { return branch(branchId).get(t); }
    public void putNode(int branchId, int t, Node n) { branch(branchId).put(t, n); }

    // ---------- пул фигур ----------
    private final List<Figure> allFigures = new ArrayList<>();
    private long nextFigureId = 1;

    // ---------- формулы ----------
    
  
  public static int itemsOnNode(int t) {
    double bonus = Math.min(20, Math.abs(t)) / 20.0;
    int n = 2 + (int) Math.round(bonus * 3);
    if (n < 1) n = 1;
    if (n > 5) n = 5;
    return n;
}

    public List<Figure> getAllFigures() { return allFigures; }

        public Figure newFigure(Figure.Shape s, int branchId, int timeAnchor) {
        Figure f = new Figure(nextFigureId++, s, branchId, timeAnchor);
        allFigures.add(f);
        return f;
    }

     public Figure newFigureWithLifespan(Figure.Shape s,
                                        int branchId, int timeAnchor, int lifespan) {
        Figure f = newFigure(s, branchId, timeAnchor);
        f.setLifespan(lifespan);
        return f;
    }

    // видимые на поле узла (branch, t)
    public List<Figure> visibleOnField(int branch, int t) {
        List<Figure> out = new ArrayList<>();
        for (Figure f : allFigures) {
            if (f.getLocation() != Figure.Location.FIELD) continue;
            if (f.visibleAt(branch, t)) out.add(f);
        }
        return out;
    }

    // ключ комбинации — ПОСЛЕДОВАТЕЛЬНОСТЬ форм в ячейках
    public static String comboKey(List<Figure> figures) {
        if (figures == null || figures.size() != Portal.CELLS) return null;
        StringBuilder sb = new StringBuilder();
        for (Figure f : figures) {
            if (f == null) return null;
            sb.append(f.getShape().ordinal());
        }
        return sb.toString();
    }

    // вспомогательный: набор комбо для ветки
    private Set<String> comboSet(int branchId) {
        return usedCombosByBranch.computeIfAbsent(branchId, k -> new HashSet<>());
    }

    public boolean isComboAvailable(int branchId, List<Figure> figures) {
        if (figures == null || figures.size() != Portal.CELLS) return false;
        String key = comboKey(figures);
        if (key == null) return false;
        return !comboSet(branchId).contains(key);
    }

    public void registerCombo(int branchId, List<Figure> figures) {
        String key = comboKey(figures);
        if (key != null) comboSet(branchId).add(key);
    }

    public void unregisterCombo(int branchId, List<Figure> figures) {
        String key = comboKey(figures);
        if (key != null) comboSet(branchId).remove(key);
    }

    // фигура в ячейке портала — по id портала и индексу
    public Figure figureInPortalCell(long portalId, int cellIndex) {
        for (Figure f : allFigures) {
            if (!f.isAlive()) continue;
            if (f.getLocation() != Figure.Location.PORTAL_CELL) continue;
            if (f.getPortalId() == portalId && f.getCellIndex() == cellIndex) return f;
        }
        return null;
    }

    // забрать фигуру из мира (глобально исчезает)
    public void pickUp(Figure f) {
        f.setAlive(false);
        f.setLocation(Figure.Location.INVENTORY);
    }

    // положить фигуру на поле
    public void dropOnField(Figure f, int branchId, int t, double angle, double radius) {
        f.setAlive(true);
        f.setLocation(Figure.Location.FIELD);
        f.setBranchIdNew(branchId);
        f.setTimeAnchor(t);
        f.setAngle(angle);
        f.setRadius(radius);
    }

    // положить фигуру в ячейку портала
    public void putInPortal(Figure f, long portalId, int cellIndex) {
        f.setLocation(Figure.Location.PORTAL_CELL);
        f.setPortalId(portalId);
        f.setCellIndex(cellIndex);
    }

    // срок жизни предмета зависит от процента его формы в узле рождения
    public static int lifespanFromPercent(double percent) {
        int base = 3;
        int extra = (int) Math.round(percent * 20.0);
        return base + extra;
    }

    private java.util.TreeSet<Integer> posTimes(int branchId) {
        return exchangerPos.computeIfAbsent(branchId, k -> new java.util.TreeSet<>());
    }

    private java.util.TreeSet<Integer> negTimes(int branchId) {
        return exchangerNeg.computeIfAbsent(branchId, k -> new java.util.TreeSet<>());
    }

    /**
     * true, если в узле (branch, t) должен быть обменник.
     * Гарантированно на +5 и −5; дальше с шагом 5..20 лет.
     * Детерминировано по seed узла.
     */
    private boolean placeExchangerAt(int branchId, int t, long seed) {
        if (t == 0) return false;

        java.util.TreeSet<Integer> pos = posTimes(branchId);
        java.util.TreeSet<Integer> neg = negTimes(branchId);

        // гарантированные
        pos.add(EXCHANGER_FIRST_T);
        neg.add(-EXCHANGER_FIRST_T);

        if (t > 0) {
            int cur = pos.last();
            while (cur < t) {
                int gap = EXCHANGER_MIN_GAP
                        + new Random(seed * 31L + cur * 17L + branchId)
                            .nextInt(EXCHANGER_MAX_GAP - EXCHANGER_MIN_GAP + 1);
                int next = cur + gap;
                if (next > t) break;
                pos.add(next);
                cur = next;
            }
            return pos.contains(t);
        } else {
            int cur = neg.first();
            while (cur > t) {
                int gap = EXCHANGER_MIN_GAP
                        + new Random(seed * 31L + cur * 17L + branchId)
                            .nextInt(EXCHANGER_MAX_GAP - EXCHANGER_MIN_GAP + 1);
                int next = cur - gap;
                if (next < t) break;
                neg.add(next);
                cur = next;
            }
            return neg.contains(t);
        }
    }

    // ---------- генерация ----------
    // ВАЖНО: лимитов ±20 больше нет. Мир есть везде.
    public Node generateNode(int t, int branchId) {
        Node existing = getNode(branchId, t);
        if (existing != null) return existing;

        long nodeSeed = seed * 31L + (long) t * 1000003L + branchId * 97L;
        Random rnd = new Random(nodeSeed);

        Node n = new Node(nextNodeId++, t, nodeSeed, branchId);

        // --- является ли узел аномальным? ---
        boolean isStart = (t == 0 && branchId == 0);
        boolean isAnomaly = !isStart && rnd.nextDouble() < 0.10;
        n.setAnomalous(isAnomaly);

        // --- стабильные параметры ветки ---
        int stableRadius = 1 + (int) (Math.abs(branchId * 31L + 7) % 5);
        double stableGlow = 0.3 + (Math.abs(branchId * 17L) % 60) / 100.0;

        n.setRadius(stableRadius);
        n.setGlow(stableGlow);
        n.setFill(0);

        // обменник в узле
        boolean hasEx = placeExchangerAt(branchId, t, nodeSeed);
        n.setHasExchanger(hasEx);

        // --- прогноз: все по нулям, заполняется зарядкой ---
        for (int i = 0; i < 5; i++) {
            n.getForecast()[i] = 0.0;
        }

        // кладём узел в мапу сразу — защита от повторного создания
        putNode(branchId, t, n);

        // --- предметы в пул ---
                 // --- предметы в пул ---
        boolean isBonusNode = !isStart && (Math.abs(t) % 10 == 0);
        int items = isAnomaly
                ? (10 + rnd.nextInt(10))
                : (isBonusNode ? itemsOnNode(t) * 2 : itemsOnNode(t));
        Figure.Shape[] allowed = allowedShapesForBranch(branchId);
              for (int i = 0; i < items; i++) {
            Figure.Shape sh = allowed[rnd.nextInt(allowed.length)];
            Figure f = newFigure(sh, branchId, t);
            f.setLifespan(lifespanFromPercent(n.getForecast()[sh.ordinal()]));
            f.setAngle(rnd.nextDouble() * 2 * Math.PI);
            f.setRadius(rnd.nextDouble() * 0.85);
            f.setLocation(Figure.Location.FIELD);
        }

        // --- порталы ---
            // --- порталы ---
        boolean isBonus = !isStart && (Math.abs(t) % 10 == 0);

        // === стартовый узел ===
        if (isStart) {
            for (int i = 0; i < 5; i++) {
                double ang = i * 2 * Math.PI / 5;
                Portal p;

                if (i == 0) {
                    p = new Portal(-1, 0, 1);
                    p.kind = Portal.Kind.TIME_PAST;
                    fillPortalWithCircles(p);
                    List<Figure> combo = new ArrayList<>();
                    for (Portal.Cell c : p.getCells()) combo.add(c.figure);
                    registerCombo(branchId, combo);
                } else if (i == 1) {
                    p = new Portal(+1, 0, 1);
                    p.kind = Portal.Kind.TIME_FUTURE;
                } else if (i == 2) {
                    p = new Portal(0, 0, 1);
                    p.kind = Portal.Kind.PARALLEL;
                    p.targetBranch = 1;
                    p.label = "⇄ ветка 1";
                    fillPortalWithCircles(p);
                } else {
                    int sign = (i == 3) ? -1 : +1;
                    int mag = 2 + rnd.nextInt(4);
                    p = new Portal(sign * mag, 0, 1 + rnd.nextInt(3));
                    p.kind = (sign < 0) ? Portal.Kind.TIME_PAST : Portal.Kind.TIME_FUTURE;
                }

                p.angle = ang;
                p.radius = R_PORTAL;
                p.setId(nextPortalId++);
                p.setFromNodeId((int) n.getId());
                p.refreshState();
                n.getPortals().add(p);
            }
            Portal.repositionAll(n.getPortals());
            currentNode = n;
            return n;
        }

        // === аномальный узел ===
        if (isAnomaly) {
            for (int i = 0; i < 2; i++) {
                double ang = i * Math.PI;
                Portal p = new Portal(0, 0, 1);
                p.anomalous = true;
                p.kind = Portal.Kind.PARALLEL;
                p.targetBranch = rnd.nextInt(20);
                p.deltaTime = rnd.nextInt(41) - 20;
                p.label = "?";

                p.angle = ang;
                p.radius = R_PORTAL;
                p.setId(nextPortalId++);
                p.setFromNodeId((int) n.getId());
                p.refreshState();
                n.getPortals().add(p);
            }
            Portal.repositionAll(n.getPortals());
            currentNode = n;
            return n;
        }

        // === обычный / бонусный узел ===
        

        // параллель — только если нет обменника и не бонусный
        boolean hasParallel = !hasEx && !isBonus && rnd.nextDouble() < 0.60;

        // аномалия — только если нет обменника (на бонусных — разрешена)
        boolean hasAnomaly = !hasEx && rnd.nextDouble() < 0.15;

        // итоговое число порталов
        int total = 5 + (hasParallel ? 1 : 0) + (hasAnomaly ? 1 : 0);

        for (int i = 0; i < total; i++) {
            double ang = i * 2 * Math.PI / total;
            Portal p;

            if (i == 0) {
                p = new Portal(-1, 0, 1);
                p.kind = Portal.Kind.TIME_PAST;
            } else if (i == 1) {
                p = new Portal(+1, 0, 1);
                p.kind = Portal.Kind.TIME_FUTURE;
            } else if (i == 5 && hasParallel) {
                p = new Portal(0, 0, 1);
                p.kind = Portal.Kind.PARALLEL;
                p.targetBranch = pickTargetBranch(t, rnd);
                p.label = "⇄ ветка " + p.targetBranch;
            } else if ((i == 6 && hasAnomaly) || (i == 5 && !hasParallel && hasAnomaly)) {
                p = new Portal(0, 0, 1);
                p.anomalous = true;
                p.kind = Portal.Kind.PARALLEL;
                p.targetBranch = rnd.nextInt(20);
                p.deltaTime = rnd.nextInt(41) - 20;
                p.label = "?";
            } else {
                int sign = rnd.nextBoolean() ? -1 : +1;
                int mag = 2 + rnd.nextInt(4);
                p = new Portal(sign * mag, 0, 1 + rnd.nextInt(3));
                p.kind = (sign < 0) ? Portal.Kind.TIME_PAST : Portal.Kind.TIME_FUTURE;
            }

            p.angle = ang;
            p.radius = R_PORTAL;
            p.setId(nextPortalId++);
            p.setFromNodeId((int) n.getId());
            p.refreshState();
            n.getPortals().add(p);
        }
        Portal.repositionAll(n.getPortals());

        currentNode = n;
        return n;
    }

    private void fillPortalWithCircles(Portal p) {
        for (int c = 0; c < Portal.CELLS; c++) {
            Figure f = new Figure(Figure.Shape.CIRCLE);
            p.getCells().get(c).figure = f;
        }
        p.refreshState();
    }

    
        /**
     * Диапазон параллельных веток зависит от t:
     *   t = 0..9   → 1..10
     *   t = 10..19 → 11..20
     *   t = 20..29 → 21..30
     * и т.д. (симметрично для t < 0)
     */
    private static int pickTargetBranch(int t, Random rnd) {
        int decade = Math.abs(t) / 10;
        int minBranch = decade * 10 + 1;
        return minBranch + rnd.nextInt(10);
    }
    
    // какие формы разрешены в ветке
    public Figure.Shape[] allowedShapesForBranch(int branchId) {
        if (branchId == 0) return new Figure.Shape[]{ Figure.Shape.CIRCLE };
        // каждая параллель — своя форма
        Figure.Shape[] all = Figure.Shape.values();
        int idx = 1 + (branchId - 1) % (all.length - 1);
        return new Figure.Shape[]{ all[idx] };
    }

    private void fillPortalFully(Random rnd, Portal p, Node n) {
        for (int c = 0; c < Portal.CELLS; c++) {
            p.getCells().get(c).figure = randFigure(rnd, n);
        }
        p.refreshState();
    }

private Figure randFigure(Random rnd, Node n) {
    Figure.Shape[] allowed = allowedShapesForBranch(n.getBranchId());
    Figure.Shape s = allowed[rnd.nextInt(allowed.length)];
    return new Figure(s);
}

 

    // ---------- годовое смещение ----------
    public void advanceYear(Player player, int direction) {
        Node n = player.getCurrentNode();
        if (n == null) return;

        long yearSeed = n.getSeed() * 131L + player.getGameYear() * 17L + direction;
        Random rnd = new Random(yearSeed);

        // проценты не деградируют сами — они были расставлены волной зарядки
        // здесь только рождение новых предметов

        // выбираем форму по прогнозу (веса = проценты)
        Figure.Shape chosen = pickShapeByForecast(rnd, n.getForecast());

        if (chosen != null
                && rnd.nextDouble() < 0.5                       // 50% шанс спавна за год
                && visibleOnField(n.getBranchId(), n.getTimeAnchor()).size() < 5) {

           Figure f = newFigure(chosen, n.getBranchId(), n.getTimeAnchor());
            f.setLifespan(lifespanFromPercent(n.getForecast()[chosen.ordinal()]));
            f.setAngle(rnd.nextDouble() * 2 * Math.PI);
            f.setRadius(rnd.nextDouble() * 0.85);
            f.setLocation(Figure.Location.FIELD);
        }

        player.incrementYear();
    }

    // случайный выбор формы по весам = процентам прогноза
    private Figure.Shape pickShapeByForecast(Random rnd, double[] forecast) {
        double sum = 0;
        for (double p : forecast) sum += p;
        if (sum <= 0) return null;   // всё по нулям — спавна нет

        double r = rnd.nextDouble() * sum;
        double acc = 0;
        for (int i = 0; i < forecast.length; i++) {
            acc += forecast[i];
            if (r <= acc) return Figure.Shape.values()[i];
        }
        return Figure.Shape.values()[forecast.length - 1];
    }

    // ---------- параллели ----------
      public void ensureBranchExists(int branchId, int fromBranch) {
        branch(branchId);
        if (getNode(branchId, 0) == null) {
            Node zero = generateNode(0, branchId);

            // back-портал только для перехода 0 → 1 (демонстрация)
            if (fromBranch == 0 && branchId == 1) {
                addParallelBackPortal(zero, fromBranch);
            }
        }
    }

    // возвратные врата: 6-й портал в параллели, ведёт обратно во fromBranch, t=0
    private void addParallelBackPortal(Node zero, int fromBranch) {
        // не дублировать
        for (Portal p : zero.getPortals()) {
            if (p.kind == Portal.Kind.PARALLEL && p.targetBranch == fromBranch) return;
        }

        Random rnd = new Random(zero.getSeed() ^ 0xABCDL);

        Portal back = new Portal(0, 0, 1);
        back.kind = Portal.Kind.PARALLEL;
        back.targetBranch = fromBranch;
        back.label = "⇦ ветка " + fromBranch;
        back.angle = -Math.PI / 2;         // временно, пересчитается
        back.radius = R_PORTAL;            // ← было R_PORTAL * 1.05
        back.setId(nextPortalId++);
        back.setFromNodeId((int) zero.getId());

        // заряжаем 5 предметами, чтобы не было ловушки
        Figure.Shape[] allowed = allowedShapesForBranch(zero.getBranchId());
        for (int c = 0; c < Portal.CELLS; c++) {
            Figure.Shape s = allowed[rnd.nextInt(allowed.length)];
            back.getCells().get(c).figure = new Figure(s);
        }
        back.refreshState();

        zero.getPortals().add(back);
        // пересчитываем ВСЕ порталы узла (теперь их 6)
        Portal.repositionAll(zero.getPortals());
    }

    // ---------- прыжок ----------
     public void jump(Player player, Portal portal) {
        if (!"open".equals(portal.getState()))
            throw new IllegalStateException("портал закрыт (" + portal.filledCells() + "/5)");

        // помечаем портал известным (до прыжка)
        player.markPortalKnown(portal);
        if (!DEBUG_INFINITE_ENERGY) {
            int cost = portal.getCostEnergy();
            if (portal.kind == Portal.Kind.TIME_PAST) {
                if (player.getPastEnergy() < cost)
                    throw new IllegalStateException("мало энергии прошлого");
                player.spendPast(cost);
            } else if (portal.kind == Portal.Kind.TIME_FUTURE) {
                if (player.getFutureEnergy() < cost)
                    throw new IllegalStateException("мало энергии будущего");
                player.spendFuture(cost);
            }
        }

        // ---------- аномалия ----------
        if (portal.anomalous) {
            // аномальный переход: в случайный узел случайной ветки
            Random rnd = new Random();
            int targetBranch = portal.targetBranch;
            int targetT = portal.deltaTime;   // может быть -20..+20

            Node target = getNode(targetBranch, targetT);
            if (target == null) target = generateNode(targetT, targetBranch);

            // обратного портала нет — аномалия односторонняя
            player.setCurrentBranch(targetBranch);
            player.setCurrentNode(target);
            currentNode = target;
            return;
        }

        // ---------- параллель ----------
           // ---------- параллель ----------
        if (portal.kind == Portal.Kind.PARALLEL) {
            int targetBranch = portal.targetBranch;
            int fromBranch = player.getCurrentBranch();
            int targetT = player.getCurrentNode().getTimeAnchor();

            ensureBranchExists(targetBranch, fromBranch);

            // прыгаем в тот же t (сохранение времени при переходе)
            Node target = getNode(targetBranch, targetT);
            if (target == null) target = generateNode(targetT, targetBranch);

            player.setCurrentBranch(targetBranch);
            player.setCurrentNode(target);
            currentNode = target;
            return;
        }

        // ---------- временной ----------
        int dt = (portal.kind == Portal.Kind.TIME_FUTURE)
                ? Math.abs(portal.deltaTime)
                : -Math.abs(portal.deltaTime);
        int t = player.getCurrentNode().getTimeAnchor() + dt;

        int sign = dt >= 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(dt); i++) advanceYear(player, sign);

        int curBranch = player.getCurrentBranch();
        Node source = player.getCurrentNode();
        Node target = getNode(curBranch, t);
        if (target == null) target = generateNode(t, curBranch);

        // связываем пару source ↔ target (если ещё не связана)

        player.setCurrentNode(target);
        currentNode = target;
    }

    // ---------- предметы и порталы ----------
    public Figure takeItemFromField(Node n, int idx) {
        if (idx < 0 || idx >= n.getItems().size()) return null;
        return n.getItems().remove(idx).figure;
    }

    public void addItemToField(Node n, Figure f, double angle, double radius) {
        n.getItems().add(new Node.ItemOnField(f, angle, radius));
    }

    public void putToPortalCell(Portal p, int cellIdx, Figure f) {
        if (cellIdx < 0 || cellIdx >= Portal.CELLS) return;
        p.getCells().get(cellIdx).figure = f;
        p.refreshState();
    }

    public Figure takeFromPortalCell(Portal p, int cellIdx) {
        if (cellIdx < 0 || cellIdx >= Portal.CELLS) return null;
        Figure f = p.getCells().get(cellIdx).figure;
        p.getCells().get(cellIdx).figure = null;
        p.refreshState();
        return f;
    }

    public void chargeForecast(Node n, int shapeIdx, Figure f) {
        n.getForecastCharged()[shapeIdx]++;
        // заряд в узле: +5%
        double[] fc = n.getForecast();
        fc[shapeIdx] = Math.min(0.80, fc[shapeIdx] + 0.05);

        // испускаем волну в будущее: +4, +3, +2, +1, 0
        int branchId = n.getBranchId();
        int t0 = n.getTimeAnchor();
        for (int step = 1; step <= 4; step++) {
            int tTarget = t0 + step;
            Node target = getNode(branchId, tTarget);
            if (target == null) target = generateNode(tTarget, branchId);
            double bonus = 0.05 - 0.01 * step;   // 4%, 3%, 2%, 1%
            double[] tf = target.getForecast();
            tf[shapeIdx] = Math.min(0.80, tf[shapeIdx] + bonus);
        }
    }

    public List<Anomaly> getAnomalies() { return anomalies; }
    public static class Anomaly {
        public int t; public int livesLeft; public boolean isWorld;
        public Anomaly(int t, int lives, boolean isWorld) {
            this.t = t; this.livesLeft = lives; this.isWorld = isWorld;
        }
    }
    private final List<Anomaly> anomalies = new ArrayList<>();
    public void tickAnomalies() {
        List<Anomaly> dead = new ArrayList<>();
        for (Anomaly a : anomalies) { a.livesLeft--; if (a.livesLeft <= 0) dead.add(a); }
        anomalies.removeAll(dead);
    }
}