package visual;

import game.Portal;

import java.awt.*;
import java.util.List;

public class CellRenderer {

    /**
     * Нарисовать одну ячейку портала.
     * Возвращает true, если что-то нарисовано из SVG.
     */
    public static boolean drawCellSvg(Graphics2D g2, int ccx, int ccy,
                                      Portal.Cell cell, VisualSpec spec) {
        if (spec == null) return false;

        String svg = (cell.figure == null) ? spec.cellEmpty : spec.cellFilled;
        if (svg == null) return false;

        // SVG рисуется от центра ячейки, 1:1
        List<SvgParser.SvgElement> els = SvgParser.parse(svg, ccx, ccy, 1.0);
        if (els.isEmpty()) return false;

        Renderer.drawSvg(g2, els);
        return true;
    }
}