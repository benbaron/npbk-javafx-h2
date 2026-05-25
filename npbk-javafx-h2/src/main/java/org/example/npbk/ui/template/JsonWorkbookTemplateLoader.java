package org.example.npbk.ui.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Loads workbook-form templates embedded as JSON resources in the application JAR. */
public final class JsonWorkbookTemplateLoader {
    private static final String TEMPLATE_BASE = "/org/example/npbk/ui/template/";

    private JsonWorkbookTemplateLoader() {
    }

    public static JsonObject load(String sheetName) {
        String resource = TEMPLATE_BASE + sheetName + ".template.json";
        try (InputStream in = JsonWorkbookTemplateLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("No embedded workbook template resource: " + resource);
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load embedded workbook template: " + resource, ex);
        }
    }
}
