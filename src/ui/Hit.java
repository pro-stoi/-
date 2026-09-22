package ui;

public class Hit {
    public enum Kind { ITEM, SLOT, PORTAL, PORTAL_CELL, ZOOM_IN, ZOOM_OUT,
                   FORECAST, NEXT_YEAR, EXCHANGER }
    public final Kind kind;
    public final int index;
    public final int x, y, r;

    public Hit(Kind k, int idx, int x, int y, int r) {
        this.kind = k; this.index = idx; this.x = x; this.y = y; this.r = r;
    }
}