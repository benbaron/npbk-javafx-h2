package org.example.npbk.ui.template;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Loads workbook-form templates embedded as gzipped/base64 JSON resources in the application JAR. */
public final class JsonWorkbookTemplateLoader {
    private static final String TEMPLATE_BASE = "/org/example/npbk/ui/template/";

    private JsonWorkbookTemplateLoader() {
    }

    public static JsonObject load(String sheetName) {
        String resource = TEMPLATE_BASE + sheetName + ".template.json.gz.b64";
        try (InputStream in = JsonWorkbookTemplateLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("No embedded workbook template resource: " + resource);
            }
            String base64 = new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("\\s+", "");
            byte[] gzippedJson = Base64.getDecoder().decode(base64);
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzippedJson))) {
                String json = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                return JsonParser.parseString(json).getAsJsonObject();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load embedded workbook template: " + resource, ex);
        }
    }
}
