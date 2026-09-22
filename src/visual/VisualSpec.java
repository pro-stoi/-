package visual;

public class VisualSpec {

    public String name = "default";
    public String background = "#0c0e16";

    // мир
    public String worldSvg = null;

    // порталы (5 состояний) — заполним на этапе 3
    public String portalClosed = null;
    public String portalOpenPast = null;
    public String portalOpenFuture = null;
    public String portalOpenParallel = null;
    public String portalOpenAnomaly = null;

    // ячейки — этап 4
    public String cellEmpty = null;
    public String cellFilled = null;

    // фигуры — этап 5
    public String figureCircle = null;
    public String figureDiamond = null;
    public String figureSquare = null;
    public String figureTriangle = null;
    public String figurePolyhedron = null;

    // обменник — этап 6
    public String exchangerSvg = null;

    // HUD — этап 7
    public String hudSvg = null;
    public String hudPosition = "center";   // center | left | right
    public int hudY = 16;
    public String hudTextColor = "#ffe6a0";
    public String hudFont = "SansSerif";
    public int hudFontSize = 13;

    // === вспомогательное ===
    /** SVG фигуры по форме (ordinal). */
    
    public String figureSvgByShape(game.Figure.Shape shape) {
        switch (shape) {
            case CIRCLE:     return figureCircle;
            case DIAMOND:    return figureDiamond;
            case SQUARE:     return figureSquare;
            case TRIANGLE:   return figureTriangle;
            case POLYHEDRON: return figurePolyhedron;
        }
        return null;
    }

    /** SVG портала по его состоянию. */
    public String portalSvgFor(game.Portal p) {
        if (!"open".equals(p.getState())) return portalClosed;
        if (p.anomalous)                  return portalOpenAnomaly;
        switch (p.kind) {
            case TIME_PAST:   return portalOpenPast;
            case TIME_FUTURE: return portalOpenFuture;
            case PARALLEL:    return portalOpenParallel;
        }
        return portalClosed;
    }

}