package visual;

import java.awt.*;
import java.util.List;

public class Renderer {

    /** Нарисовать список SVG-элементов. */
    public static void drawSvg(Graphics2D g2, List<SvgParser.SvgElement> elements) {
        if (elements == null) return;
        for (SvgParser.SvgElement el : elements) {
            if (el.shape == null) continue;

            Composite oldComposite = g2.getComposite();
            if (el.opacity < 1.0) {
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, (float) el.opacity));
            }

            // заливка
            if (el.fill != null) {
                g2.setColor(el.fill);
                g2.fill(el.shape);
            }
            // контур
            if (el.stroke != null && el.strokeWidth > 0) {
                g2.setColor(el.stroke);
                g2.setStroke(new BasicStroke(el.strokeWidth));
                g2.draw(el.shape);
                g2.setStroke(new BasicStroke(1f));
            }

            g2.setComposite(oldComposite);
        }
    }

    /** Разбор SVG + отрисовка — короткий путь для одного элемента. */
    public static void drawSvgString(Graphics2D g2, String svg,
                                     int cx, int cy, double scale) {
        if (svg == null) return;
        List<SvgParser.SvgElement> els = SvgParser.parse(svg, cx, cy, scale);
        drawSvg(g2, els);
    }
}
