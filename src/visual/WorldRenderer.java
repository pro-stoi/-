package visual;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class WorldRenderer {

    /**
     * Нарисовать тело мира.
     * @return true, если что-то нарисовано из SVG. false — рисуй fallback сам.
     */
    public static boolean drawSvgWorld(Graphics2D g2, int cx, int cy, int worldR,
                                       VisualSpec spec, Color glow, Color edge,
                                       double glowStrength) {
        if (spec == null || spec.worldSvg == null) return false;

        // свечение — 6 слоёв (как раньше), поверх него — SVG
        for (int i = 6; i >= 1; i--) {
            int alpha = (int) (glowStrength * 40 / i);
            g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(),
                                  Math.max(4, alpha)));
            int rr = worldR + i * 8;
            g2.fill(new Ellipse2D.Double(cx - rr, cy - rr, rr * 2, rr * 2));
        }

        // SVG рисуем с масштабом: r=100 в SVG = worldR в пикселях
        double scale = worldR / 100.0;
        List<SvgParser.SvgElement> els = SvgParser.parse(spec.worldSvg, cx, cy, scale);

        // если у элемента нет fill — подставим тёмный фон мира
        for (SvgParser.SvgElement el : els) {
            if (el.fill == null && el.stroke == null) {
                el.fill = new Color(30, 40, 70);
            }
        }
        Renderer.drawSvg(g2, els);
        return true;
    }
}