package visual;

import game.Portal;

import java.awt.*;
import java.util.List;

public class PortalRenderer {

    /**
     * Нарисовать один портал по спеке.
     * Возвращает true, если что-то нарисовано из SVG.
     */
    public static boolean drawPortalSvg(Graphics2D g2, int px, int py,
                                        Portal p, VisualSpec spec) {
        if (spec == null) return false;

        String svg = spec.portalSvgFor(p);
        if (svg == null) return false;

        // SVG рисуется от центра портала, 1:1
        List<SvgParser.SvgElement> els = SvgParser.parse(svg, px, py, 1.0);
        if (els.isEmpty()) return false;

        Renderer.drawSvg(g2, els);
        return true;
    }
}