package game;

public class Figure {

    public enum Shape { CIRCLE, DIAMOND, SQUARE, TRIANGLE, POLYHEDRON }
   
    public enum Location { FIELD, PORTAL_CELL, INVENTORY }

    // --- новое ---
    private final long id;
    private int branchIdNew;         // ветка, где живёт
    private int timeAnchor;          // где родилась
    private int lifespan = 5;        // сколько лет живёт от timeAnchor
    private boolean alive = true;    // false = забрана навсегда
    private Location location = Location.FIELD;
    private double angle;            // для FIELD
    private double radius;           // для FIELD
    private long portalId = -1;      // для PORTAL_CELL
    private int cellIndex = -1;      // для PORTAL_CELL
    private int slotIndex = -1;      // для INVENTORY

    // --- старое (пока живёт, чтобы не ломать UI) ---
    private final Shape shape;
    
    private String owner;
    private int branchId;
    private int slot;
    private int cell;
    private long nodeId;

  
    public Figure(Shape shape) {
        this.id = -1;
        this.shape = shape;
        this.owner = "world";
    }

    public Figure(long id, Shape shape, int branchIdNew, int timeAnchor) {
        this.id = id;
        this.shape = shape;
        this.owner = "world";
        this.branchIdNew = branchIdNew;
        this.timeAnchor = timeAnchor;
    }
    // --- новое ---
    public long getId() { return id; }
    public int getBranchIdNew() { return branchIdNew; }
    public void setBranchIdNew(int b) { this.branchIdNew = b; }
    public int getTimeAnchor() { return timeAnchor; }
    public void setTimeAnchor(int t) { this.timeAnchor = t; }
    public int getLifespan() { return lifespan; }
    public void setLifespan(int l) { this.lifespan = l; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean a) { this.alive = a; }
    public Location getLocation() { return location; }
    public void setLocation(Location l) { this.location = l; }
    public double getAngle() { return angle; }
    public void setAngle(double a) { this.angle = a; }
    public double getRadius() { return radius; }
    public void setRadius(double r) { this.radius = r; }
    public long getPortalId() { return portalId; }
    public void setPortalId(long p) { this.portalId = p; }
    public int getCellIndex() { return cellIndex; }
    public void setCellIndex(int c) { this.cellIndex = c; }
    public int getSlotIndex() { return slotIndex; }
    public void setSlotIndex(int s) { this.slotIndex = s; }

    // видна ли фигура в узле (branch, t)
    public boolean visibleAt(int branch, int t) {
        if (!alive) return false;
        if (branchIdNew != branch) return false;
        return t >= timeAnchor && t <= timeAnchor + lifespan;
    }

    // --- старое ---
    public Shape getShape() { return shape; }
  
    public String getOwner() { return owner; }
    public void setOwner(String o) { this.owner = o; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int b) { this.branchId = b; }
    public int getSlot() { return slot; }
    public void setSlot(int s) { this.slot = s; }
    public int getCell() { return cell; }
    public void setCell(int c) { this.cell = c; }
    public long getNodeId() { return nodeId; }
    public void setNodeId(long n) { this.nodeId = n; }

    public String symbol() {
        switch (shape) {
            case CIRCLE:     return "O";
            case DIAMOND:    return "<>";
            case SQUARE:     return "[]";
            case TRIANGLE:   return "/\\";
            case POLYHEDRON: return "*";
        }
        return "?";
    }

      @Override
    public String toString() {
        return symbol() + "[" + shape + "]";
    }
    public static Shape nextShape(Shape s) {
    Shape[] all = Shape.values();
    return all[(s.ordinal() + 1) % all.length];
}

public static Shape prevShape(Shape s) {
    Shape[] all = Shape.values();
    return all[(s.ordinal() - 1 + all.length) % all.length];
}
}