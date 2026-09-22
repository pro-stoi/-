package visual;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgParser {

    /** Один элемент SVG: форма + цвета + id. */
    public static class SvgElement {
        public String id;
        public Shape shape;
        public Color fill;
        public Color stroke;
        public float strokeWidth = 0f;
        public double opacity = 1.0;
    }

    // ===== публичный вход =====
    public static List<SvgElement> parse(String svg, int cx, int cy, double scale) {
        List<SvgElement> out = new ArrayList<>();
        if (svg == null || svg.isEmpty()) return out;

        // вытаскиваем содержимое <svg>...</svg>
        int start = svg.indexOf('>');
        int end = svg.lastIndexOf("</svg>");
        if (start < 0 || end < 0) return out;
        String body = svg.substring(start + 1, end);

        // ищем все теги вида <circle .../>, <path .../>, ...
        Pattern p = Pattern.compile("<(circle|rect|ellipse|line|polygon|path)\\b([^>]*)/?>",
                                    Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(body);
        while (m.find()) {
            String tag = m.group(1).toLowerCase();
            String attrs = m.group(2);
            SvgElement el = parseElement(tag, attrs, cx, cy, scale);
            if (el != null) out.add(el);
        }
        return out;
    }

    // ===== парсинг одного элемента =====
    private static SvgElement parseElement(String tag, String attrs,
                                           int cx, int cy, double scale) {
        SvgElement el = new SvgElement();
        el.id = attr(attrs, "id");
        el.fill = parseColor(attr(attrs, "fill"), null);
        el.stroke = parseColor(attr(attrs, "stroke"), null);
        el.strokeWidth = (float) parseDouble(attr(attrs, "stroke-width"), 0);
        el.opacity = parseDouble(attr(attrs, "opacity"), 1.0);

        switch (tag) {
            case "circle": {
                double x = cx + parseDouble(attr(attrs, "cx"), 0) * scale;
                double y = cy + parseDouble(attr(attrs, "cy"), 0) * scale;
                double r = parseDouble(attr(attrs, "r"), 0) * scale;
                el.shape = new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
                break;
            }
            case "ellipse": {
                double x = cx + parseDouble(attr(attrs, "cx"), 0) * scale;
                double y = cy + parseDouble(attr(attrs, "cy"), 0) * scale;
                double rx = parseDouble(attr(attrs, "rx"), 0) * scale;
                double ry = parseDouble(attr(attrs, "ry"), 0) * scale;
                el.shape = new Ellipse2D.Double(x - rx, y - ry, rx * 2, ry * 2);
                break;
            }
            case "rect": {
                double x = cx + parseDouble(attr(attrs, "x"), 0) * scale;
                double y = cy + parseDouble(attr(attrs, "y"), 0) * scale;
                double w = parseDouble(attr(attrs, "width"), 0) * scale;
                double h = parseDouble(attr(attrs, "height"), 0) * scale;
                double rx = parseDouble(attr(attrs, "rx"), 0) * scale;
                double ry = parseDouble(attr(attrs, "ry"), 0) * scale;
                if (rx > 0 || ry > 0)
                    el.shape = new RoundRectangle2D.Double(x, y, w, h, rx, ry);
                else
                    el.shape = new Rectangle2D.Double(x, y, w, h);
                break;
            }
            case "line": {
                double x1 = cx + parseDouble(attr(attrs, "x1"), 0) * scale;
                double y1 = cy + parseDouble(attr(attrs, "y1"), 0) * scale;
                double x2 = cx + parseDouble(attr(attrs, "x2"), 0) * scale;
                double y2 = cy + parseDouble(attr(attrs, "y2"), 0) * scale;
                el.shape = new Line2D.Double(x1, y1, x2, y2);
                break;
            }
            case "polygon":
            case "polyline": {
                String pts = attr(attrs, "points");
                if (pts == null) return null;
                Path2D.Double path = new Path2D.Double();
                String[] pairs = pts.trim().split("\\s+");
                for (int i = 0; i < pairs.length; i++) {
                    String[] xy = pairs[i].split(",");
                    if (xy.length < 2) continue;
                    double x = cx + Double.parseDouble(xy[0]) * scale;
                    double y = cy + Double.parseDouble(xy[1]) * scale;
                    if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                }
                if (tag.equals("polygon")) path.closePath();
                el.shape = path;
                break;
            }
            case "path": {
                String d = attr(attrs, "d");
                if (d == null) return null;
                el.shape = parsePath(d, cx, cy, scale);
                break;
            }
            default:
                return null;
        }
        return el;
    }

    // ===== упрощённый path: поддерживаем M, L, Z, H, V (только абсолютные) =====
    private static Shape parsePath(String d, int cx, int cy, double scale) {
        Path2D.Double p = new Path2D.Double();
        // токенизация: разделяем по командам и числам
        String[] tokens = d.replace(",", " ").trim().split("\\s+");
        double x = 0, y = 0;
        double startX = 0, startY = 0;
        char cmd = 0;
        int i = 0;
        while (i < tokens.length) {
            String t = tokens[i];
            if (t.length() == 1 && Character.isLetter(t.charAt(0))) {
                cmd = t.charAt(0);
                i++;
                continue;
            }
            switch (cmd) {
                case 'M':
                    if (i + 1 >= tokens.length) break;
                    x = Double.parseDouble(tokens[i++]);
                    y = Double.parseDouble(tokens[i++]);
                    p.moveTo(cx + x * scale, cy + y * scale);
                    startX = x; startY = y;
                    cmd = 'L';   // дальше идут L
                    break;
                case 'L':
                    if (i + 1 >= tokens.length) break;
                    x = Double.parseDouble(tokens[i++]);
                    y = Double.parseDouble(tokens[i++]);
                    p.lineTo(cx + x * scale, cy + y * scale);
                    break;
                case 'H':
                    x = Double.parseDouble(tokens[i++]);
                    p.lineTo(cx + x * scale, cy + y * scale);
                    break;
                case 'V':
                    y = Double.parseDouble(tokens[i++]);
                    p.lineTo(cx + x * scale, cy + y * scale);
                    break;
                case 'Z':
                case 'z':
                    p.closePath();
                    x = startX; y = startY;
                    i++;
                    break;
                default:
                    // неизвестная команда — прерываем
                    return p;
            }
        }
        return p;
    }

    // ===== утилиты =====
    private static String attr(String attrs, String name) {
        Pattern p = Pattern.compile(name + "\\s*=\\s*[\"']([^\"']*)[\"']");
        Matcher m = p.matcher(attrs);
        return m.find() ? m.group(1) : null;
    }

    private static double parseDouble(String s, double def) {
        if (s == null) return def;
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return def; }
    }

    /** Цвет: "#rrggbb", "#rgb", "none", null. */
    private static Color parseColor(String s, Color def) {
        if (s == null) return def;
        s = s.trim();
        if (s.equalsIgnoreCase("none") || s.isEmpty()) return null;
        if (!s.startsWith("#")) return def;
        try {
            String hex = s.substring(1);
            if (hex.length() == 3) {
                int r = Integer.parseInt("" + hex.charAt(0) + hex.charAt(0), 16);
                int g = Integer.parseInt("" + hex.charAt(1) + hex.charAt(1), 16);
                int b = Integer.parseInt("" + hex.charAt(2) + hex.charAt(2), 16);
                return new Color(r, g, b);
            }
            if (hex.length() == 6) {
                return new Color(
                        Integer.parseInt(hex.substring(0, 2), 16),
                        Integer.parseInt(hex.substring(2, 4), 16),
                        Integer.parseInt(hex.substring(4, 6), 16));
            }
        } catch (Exception e) { /* ignore */ }
        return def;
    }
}