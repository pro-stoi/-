package ui;

import java.awt.Color;

public class BranchSkin {

    public final Color glow;       // свечение мира
    public final Color worldEdge;  // обводка мира
    public final Color past;       // портал в прошлое
    public final Color future;     // портал в будущее
    public final Color parallel;   // портал в параллель
    public final Color anomaly;    // аномальный портал
    public final Color shapeTint;  // лёгкий оттенок фигур
    public final Color bg;         // фон канвы

    private BranchSkin(Color glow, Color worldEdge, Color past, Color future,
                       Color parallel, Color anomaly, Color shapeTint, Color bg) {
        this.glow = glow;
        this.worldEdge = worldEdge;
        this.past = past;
        this.future = future;
        this.parallel = parallel;
        this.anomaly = anomaly;
        this.shapeTint = shapeTint;
        this.bg = bg;
    }

    // палитры для 8 веток (циклично)
    private static final BranchSkin[] PRESETS = new BranchSkin[] {
        // 0 — синяя (домашняя)
        new BranchSkin(
            new Color(90, 160, 255), new Color(120, 180, 255),
            new Color(90, 180, 255), new Color(255, 170, 90),
            new Color(160, 140, 255), new Color(200, 120, 255),
            new Color(255, 255, 255), new Color(12, 14, 22)),

        // 1 — фиолетовая
        new BranchSkin(
            new Color(180, 120, 255), new Color(210, 150, 255),
            new Color(160, 100, 255), new Color(255, 140, 220),
            new Color(220, 130, 255), new Color(255, 100, 255),
            new Color(255, 220, 255), new Color(20, 12, 28)),

        // 2 — зелёная
        new BranchSkin(
            new Color(90, 220, 140), new Color(140, 240, 180),
            new Color(60, 200, 160), new Color(200, 240, 90),
            new Color(120, 255, 180), new Color(180, 255, 120),
            new Color(220, 255, 220), new Color(12, 20, 16)),

        // 3 — красная
        new BranchSkin(
            new Color(255, 120, 120), new Color(255, 150, 150),
            new Color(255, 90, 90), new Color(255, 180, 100),
            new Color(255, 140, 180), new Color(255, 90, 140),
            new Color(255, 230, 230), new Color(22, 12, 14)),

        // 4 — бирюзовая
        new BranchSkin(
            new Color(90, 220, 220), new Color(130, 240, 240),
            new Color(60, 200, 220), new Color(140, 240, 200),
            new Color(140, 220, 255), new Color(200, 240, 255),
            new Color(220, 255, 255), new Color(12, 20, 22)),

        // 5 — золотая
        new BranchSkin(
            new Color(255, 200, 90), new Color(255, 220, 140),
            new Color(255, 190, 80), new Color(255, 230, 120),
            new Color(255, 200, 160), new Color(255, 180, 60),
            new Color(255, 250, 220), new Color(22, 18, 10)),

        // 6 — розовая
        new BranchSkin(
            new Color(255, 130, 190), new Color(255, 160, 210),
            new Color(255, 110, 180), new Color(255, 170, 200),
            new Color(255, 150, 220), new Color(255, 100, 160),
            new Color(255, 230, 240), new Color(22, 12, 20)),

        // 7 — серо-стальная
        new BranchSkin(
            new Color(180, 200, 220), new Color(210, 225, 240),
            new Color(160, 190, 220), new Color(220, 200, 180),
            new Color(200, 200, 220), new Color(230, 230, 240),
            new Color(240, 245, 255), new Color(16, 18, 22)),
    };

    public static BranchSkin of(int branchId) {
        int idx = Math.floorMod(branchId, PRESETS.length);
        return PRESETS[idx];
    }
}