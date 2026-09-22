package visual;

import game.Figure;

import java.awt.*;
import java.util.List;

public class FigureRenderer {

    /**
     * Нарисовать фигуру по спеке.
     * @param size  желаемый радиус (в пикселях), в SVG r=18, значит scale = size/18.
     * @return true, если нарисовано из SVG.
     */
    public static boolean drawFigureSvg(Graphics2D g2, Figure.Shape shape,
                                        int x, int y, int size, VisualSpec spec) {
        if (spec == null || shape == null) return false;

        String svg = spec.figureSvgByShape(shape);
        if (svg == null) return false;

        double scale = size / 18.0;
        List<SvgParser.SvgElement> els = SvgParser.parse(svg, x, y, scale);
        if (els.isEmpty()) return false;

        Renderer.drawSvg(g2, els);
        return true;
    }
}