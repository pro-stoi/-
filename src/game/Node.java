package game;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private long id;
    private int timeAnchor;
    private long seed;
    private int branchId;
    private String name;

    // одинаковые во времени — цвета/размеры одинаковы в одной ветке
    private int radius;
    private double glow;
    private int fill;

    // прогноз по 5 формам
    private final int[] forecastCharged = new int[5];
    private final double[] forecast = new double[5];
    public final int[] forecastYears = new int[5];
    public final double[] forecastBasePct = new double[5];

    
      private boolean anomalous = false;
    public boolean isAnomalous() { return anomalous; }
    public void setAnomalous(boolean a) { this.anomalous = a; }
    
        // обменник в узле
    private boolean hasExchanger = false;
    private double exchangerAngle = 0.5 * Math.PI;   // 90° — снизу
    private double exchangerRadius = 0.65;

    public boolean hasExchanger() { return hasExchanger; }
    public void setHasExchanger(boolean b) { this.hasExchanger = b; }
    public double getExchangerAngle() { return exchangerAngle; }
    public void setExchangerAngle(double a) { this.exchangerAngle = a; }
    public double getExchangerRadius() { return exchangerRadius; }
    public void setExchangerRadius(double r) { this.exchangerRadius = r; }
    
    
    public static class ItemOnField {
        public Figure figure;
        public double angle;
        public double radius;
        public ItemOnField(Figure f, double a, double r) {
            this.figure = f; this.angle = a; this.radius = r;
        }
    }

    private final List<ItemOnField> items = new ArrayList<>();
    private final List<Portal> portals = new ArrayList<>();

    public Node(long id, int timeAnchor, long seed, int branchId) {
        this.id = id;
        this.timeAnchor = timeAnchor;
        this.seed = seed;
        this.branchId = branchId;
        this.name = "t=" + (timeAnchor >= 0 ? "+" : "") + timeAnchor;
    }

    public long getId() { return id; }
    public int getTimeAnchor() { return timeAnchor; }
    public long getSeed() { return seed; }
    public int getBranchId() { return branchId; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public int getRadius() { return radius; }
    public void setRadius(int r) { this.radius = r; }
    public double getGlow() { return glow; }
    public void setGlow(double g) { this.glow = g; }
    public int getFill() { return fill; }
    public void setFill(int f) { this.fill = f; }

    public List<ItemOnField> getItems() { return items; }
    public List<Portal> getPortals() { return portals; }
    public double[] getForecast() { return forecast; }
    public int[] getForecastCharged() { return forecastCharged; }

    @Override
    public String toString() {
        return name + " (ветка " + branchId + ", предметов " + items.size()
                + ", порталов " + portals.size() + ")";
    }

    
    
}