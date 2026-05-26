package org.example.npbk.report;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Values supplied to semantic report templates. */
public class ReportValueSet {
    private final Map<String, Object> scalars = new LinkedHashMap<>();
    private final Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();

    public void put(String key, Object value) {
        scalars.put(key, value);
    }

    public void putMoney(String key, BigDecimal value) {
        scalars.put(key, value == null ? BigDecimal.ZERO : value);
    }

    public Object get(String key) {
        return scalars.get(key);
    }

    public Map<String, Object> scalars() {
        return scalars;
    }

    public void putTable(String key, List<Map<String, Object>> rows) {
        tables.put(key, rows == null ? List.of() : rows);
    }

    public List<Map<String, Object>> table(String key) {
        return tables.getOrDefault(key, List.of());
    }

    public Map<String, List<Map<String, Object>>> tables() {
        return tables;
    }
}
