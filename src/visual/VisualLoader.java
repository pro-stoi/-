package visual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualLoader {

    /** Корень визуала. */
    public static final String DIR = "assets";

    // =========================================================
    //  ПУБЛИЧНОЕ API
    // =========================================================

    public static VisualSpec loadSkin(String worldName,
                                      String portalsName,
                                      String cellsName,
                                      String figuresName) {
        VisualSpec spec = new VisualSpec();
        spec.name = worldName + "/" + portalsName + "/" + cellsName + "/" + figuresName;

        loadWorld(spec, worldName);
        loadPortals(spec, portalsName);
        loadCells(spec, cellsName);
        loadFigures(spec, figuresName);

        return spec;
    }

    public static List<String> listAll(String sub) {
        List<String> out = new ArrayList<>();
        Path dir = Paths.get(DIR, sub);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return out;
        try {
            Files.list(dir)
                 .filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().endsWith(".json"))
                 .forEach(p -> {
                     String n = p.getFileName().toString();
                     out.add(n.substring(0, n.length() - 5));
                 });
        } catch (IOException e) {
            System.err.println("[VisualLoader] listAll " + sub + ": " + e.getMessage());
        }
        Collections.sort(out);
        return out;
    }

    // =========================================================
    //  ЗАГРУЗЧИКИ ЧАСТЕЙ
    // =========================================================

    private static void loadWorld(VisualSpec spec, String name) {
        String json = readJson("worlds", name);
        if (json == null) return;
        spec.background = jsonString(json, "background", spec.background);
        spec.worldSvg   = jsonString(json, "svg", null);
    }

    private static void loadPortals(VisualSpec spec, String name) {
        String json = readJson("portals", name);
        if (json == null) return;
        spec.portalClosed       = jsonString(json, "closed", null);
        spec.portalOpenPast     = jsonString(json, "open_past", null);
        spec.portalOpenFuture   = jsonString(json, "open_future", null);
        spec.portalOpenParallel = jsonString(json, "open_parallel", null);
        spec.portalOpenAnomaly  = jsonString(json, "open_anomaly", null);
    }

    private static void loadCells(VisualSpec spec, String name) {
        String json = readJson("cells", name);
        if (json == null) return;
        spec.cellEmpty  = jsonString(json, "empty", null);
        spec.cellFilled = jsonString(json, "filled", null);
    }

    private static void loadFigures(VisualSpec spec, String name) {
        String json = readJson("figures", name);
        if (json == null) return;
        spec.figureCircle     = jsonString(json, "CIRCLE", null);
        spec.figureDiamond    = jsonString(json, "DIAMOND", null);
        spec.figureSquare     = jsonString(json, "SQUARE", null);
        spec.figureTriangle   = jsonString(json, "TRIANGLE", null);
        spec.figurePolyhedron = jsonString(json, "POLYHEDRON", null);
    }

    // =========================================================
    //  РУЧНОЙ ПАРСЕР JSON (без regex!)
    // =========================================================

    /**
     * Достать строковое значение по ключу. Возвращает null если не найдено.
     * Поддерживает экранирование \" \\ \n \t \r.
     */
    private static String jsonString(String json, String key, String def) {
        if (json == null) return def;

        int pos = 0;
        int len = json.length();

        while (pos < len) {
            // 1. Пропускаем пробелы и любые символы до кавычки
            int qStart = json.indexOf('"', pos);
            if (qStart < 0) return def;

            // 2. Читаем ключ (до закрывающей кавычки)
            int qEnd = findStringEnd(json, qStart + 1);
            if (qEnd < 0) return def;

            String currentKey = unescape(json.substring(qStart + 1, qEnd));

            // 3. Пропускаем пробелы до ':'
            int colon = qEnd + 1;
            while (colon < len && Character.isWhitespace(json.charAt(colon))) colon++;
            if (colon >= len || json.charAt(colon) != ':') {
                pos = qEnd + 1;
                continue;
            }

            // 4. Пропускаем пробелы после ':'
            int valStart = colon + 1;
            while (valStart < len && Character.isWhitespace(json.charAt(valStart))) valStart++;
            if (valStart >= len) return def;

            // 5. Если значение — строка
            if (json.charAt(valStart) == '"') {
                int valEnd = findStringEnd(json, valStart + 1);
                if (valEnd < 0) return def;

                if (currentKey.equals(key)) {
                    return unescape(json.substring(valStart + 1, valEnd));
                }

                pos = valEnd + 1;
            } else {
                // не строка (число, объект, массив) — пропускаем значение
                int skipEnd = skipValue(json, valStart);
                if (skipEnd < 0) return def;
                pos = skipEnd;
            }
        }

        return def;
    }

    /** Найти закрывающую кавычку строки, учитывая экранирование. */
    private static int findStringEnd(String s, int start) {
        int i = start;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                i += 2;  // пропускаем экранированный символ
                continue;
            }
            if (c == '"') return i;
            i++;
        }
        return -1;
    }

    /** Пропустить значение (число, объект, массив, true/false/null). */
    private static int skipValue(String s, int start) {
        int i = start;
        int depth = 0;
        boolean inStr = false;

        while (i < s.length()) {
            char c = s.charAt(i);

            if (inStr) {
                if (c == '\\') { i += 2; continue; }
                if (c == '"') inStr = false;
                i++;
                continue;
            }

            if (c == '"') { inStr = true; i++; continue; }
            if (c == '{' || c == '[') { depth++; i++; continue; }
            if (c == '}' || c == ']') {
                if (depth == 0) return i;
                depth--;
                i++;
                if (depth == 0) return i;
                continue;
            }
            if (c == ',' && depth == 0) return i;

            i++;
        }
        return -1;
    }

    private static String unescape(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case 'r':  sb.append('\r'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    default:   sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String readJson(String sub, String name) {
        if (name == null) return null;
        Path p = Paths.get(DIR, sub, name + ".json");
        if (!Files.exists(p)) {
            System.err.println("[VisualLoader] нет файла: " + p.toAbsolutePath());
            return null;
        }
        try {
            return new String(Files.readAllBytes(p), "UTF-8");
        } catch (IOException e) {
            System.err.println("[VisualLoader] не читается " + p + ": " + e.getMessage());
            return null;
        }
    }
}