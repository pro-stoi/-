package visual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualRegistry {

    private static final Map<Integer, VisualSpec> CACHE = new HashMap<>();
    private static long worldSeed = 0;

    public static void setWorldSeed(long seed) { worldSeed = seed; }

    /** Вернуть спек для ветки. Ветка 0 — фиксированный дом. */
    public static VisualSpec forBranch(int branchId) {
        if (CACHE.containsKey(branchId)) return CACHE.get(branchId);
        VisualSpec spec = loadForBranch(branchId);
        CACHE.put(branchId, spec);
        return spec;
    }

    private static VisualSpec loadForBranch(int branchId) {
        // === ветка 0 — фиксированный дом ===
        if (branchId == 0) {
            return VisualLoader.loadSkin("w_default", "p_default", "c_default", "f_default");
        }

        // === остальные — случайные комбинации из пулов ===
        List<String> worlds  = VisualLoader.listAll("worlds");
        List<String> portals = VisualLoader.listAll("portals");
        List<String> cells   = VisualLoader.listAll("cells");
        List<String> figures = VisualLoader.listAll("figures");

        String w = pick(worlds,  branchId, "world");
        String p = pick(portals, branchId, "portals");
        String c = pick(cells,   branchId, "cells");
        String f = pick(figures, branchId, "figures");

        System.out.println("[VisualRegistry] ветка " + branchId
                + " → world=" + w + " portals=" + p
                + " cells=" + c + " figures=" + f);

        VisualSpec spec = VisualLoader.loadSkin(w, p, c, f);
        if (spec == null) {
            spec = new VisualSpec();
            spec.name = "fallback";
        }
        return spec;
    }

    /** Выбор по hash(seed, branchId, tag). */
    private static String pick(List<String> list, int branchId, String tag) {
        if (list == null || list.isEmpty()) return null;
        long h = worldSeed * 31L + branchId * 1000003L + tag.hashCode();
        int idx = (int) Math.floorMod(h, list.size());
        return list.get(idx);
    }

    public static void clearCache() { CACHE.clear(); }
}