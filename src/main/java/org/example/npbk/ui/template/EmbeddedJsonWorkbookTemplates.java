package org.example.npbk.ui.template;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Loads embedded JSON workbook-form templates from compressed resource parts. */
public final class EmbeddedJsonWorkbookTemplates {
    private static final String RESOURCE_BASE = "/org/example/npbk/ui/template/workbook-report-templates.part";
    private static final int RESOURCE_PART_COUNT = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode bundle;

    private EmbeddedJsonWorkbookTemplates() {
    }

    public static JsonNode bundle() {
        if (bundle == null) {
            bundle = loadBundle();
        }
        return bundle;
    }

    public static JsonNode template(String sheetName) {
        JsonNode node = bundle().path("templates").path(sheetName);
        if (node.isMissingNode()) {
            throw new IllegalArgumentException("No embedded workbook-form template for sheet: " + sheetName);
        }
        return node;
    }

    private static JsonNode loadBundle() {
        String encoded = loadEncodedParts();
        try {
            byte[] gz = Base64.getDecoder().decode(encoded);
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
                return MAPPER.readTree(in.readAllBytes());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load embedded workbook-form templates", ex);
        }
    }

    private static String loadEncodedParts() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RESOURCE_PART_COUNT; i++) {
            String resource = RESOURCE_BASE + "%02d".formatted(i) + ".txt";
            try (InputStream in = EmbeddedJsonWorkbookTemplates.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("Missing embedded workbook-form template resource: " + resource);
                }
                sb.append(new String(in.readAllBytes(), StandardCharsets.US_ASCII).trim());
            } catch (IOException ex) {
                throw new IllegalStateException("Could not read embedded workbook-form template resource: " + resource, ex);
            }
        }
        return sb.toString();
    }
}
