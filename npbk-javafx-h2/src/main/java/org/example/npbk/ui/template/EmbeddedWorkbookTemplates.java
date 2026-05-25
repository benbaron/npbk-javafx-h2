package org.example.npbk.ui.template;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Embedded workbook-form templates extracted from
 * {@code SCA Exchequer Report - 2026-03-D(1).xlsx}.
 *
 * The application does not load the XLSX at runtime.  The workbook is used as a
 * source artifact; extracted cell positions, row/column sizes, merge regions,
 * displayed cell text, and style hints are packaged as compressed resources.
 */
public final class EmbeddedWorkbookTemplates {
    private static final String RESOURCE_BASE = "/org/example/npbk/ui/template/workbook-template.part";
    private static final int RESOURCE_PART_COUNT = 6;

    private static TemplateBundle cached;

    private EmbeddedWorkbookTemplates() {
    }

    public record CellSpec(int row, int col, String text, int styleId) {
    }

    public record MergeSpec(int row, int col, int rowSpan, int colSpan) {
    }

    public record SheetTemplate(
            String sheetName,
            String title,
            String sourceRange,
            int[] rowHeights,
            int[] colWidths,
            List<MergeSpec> merges,
            List<CellSpec> cells
    ) {
    }

    public record TemplateBundle(
            String sourceSha256,
            Map<Integer, String> styles,
            Map<String, SheetTemplate> sheets
    ) {
    }

    public static TemplateBundle bundle() {
        if (cached == null) {
            cached = parse();
        }
        return cached;
    }

    public static SheetTemplate byName(String sheetName) {
        SheetTemplate template = bundle().sheets().get(sheetName);
        if (template == null) {
            throw new IllegalArgumentException("No embedded workbook template for sheet: " + sheetName);
        }
        return template;
    }

    public static String styleCss(int styleId) {
        return bundle().styles().getOrDefault(styleId, "-fx-padding: 2 4 2 4;");
    }

    private static TemplateBundle parse() {
        String data = inflate(loadCompressedBase64());
        Map<Integer, String> styles = new HashMap<>();
        Map<String, SheetTemplate> sheets = new LinkedHashMap<>();

        String sha = "";
        String sheetName = null;
        String title = null;
        String range = null;
        int[] rows = new int[0];
        int[] cols = new int[0];
        List<MergeSpec> merges = new ArrayList<>();
        List<CellSpec> cells = new ArrayList<>();

        for (String line : data.split("\\n")) {
            if (line.isEmpty()) {
                continue;
            }
            String[] p = line.split("\\t", -1);
            switch (p[0]) {
                case "STYLE" -> styles.put(Integer.parseInt(p[1]), decode(p[2]));
                case "HASH" -> sha = p[1];
                case "T" -> {
                    sheetName = decode(p[1]);
                    title = decode(p[2]);
                    range = decode(p[3]);
                    rows = new int[0];
                    cols = new int[0];
                    merges = new ArrayList<>();
                    cells = new ArrayList<>();
                }
                case "R" -> rows = ints(p[1]);
                case "C" -> cols = ints(p[1]);
                case "M" -> merges.add(new MergeSpec(
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]),
                        Integer.parseInt(p[4])
                ));
                case "X" -> cells.add(new CellSpec(
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[2]),
                        decode(p[4]),
                        Integer.parseInt(p[3])
                ));
                case "E" -> sheets.put(sheetName, new SheetTemplate(
                        sheetName,
                        title,
                        range,
                        rows,
                        cols,
                        List.copyOf(merges),
                        List.copyOf(cells)
                ));
                default -> throw new IllegalStateException("Unknown embedded workbook template line: " + p[0]);
            }
        }

        return new TemplateBundle(sha, Map.copyOf(styles), Map.copyOf(sheets));
    }

    private static int[] ints(String csv) {
        if (csv == null || csv.isEmpty()) {
            return new int[0];
        }
        String[] parts = csv.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            out[i] = Integer.parseInt(parts[i]);
        }
        return out;
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static String loadCompressedBase64() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RESOURCE_PART_COUNT; i++) {
            String resource = RESOURCE_BASE + "%02d".formatted(i) + ".txt";
            try (InputStream in = EmbeddedWorkbookTemplates.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("Missing embedded template resource: " + resource);
                }
                sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8).trim());
            } catch (IOException ex) {
                throw new IllegalStateException("Could not read embedded template resource " + resource, ex);
            }
        }
        return sb.toString();
    }

    private static String inflate(String base64) {
        try {
            byte[] gz = Base64.getDecoder().decode(base64);
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not inflate embedded workbook templates", ex);
        }
    }
}
