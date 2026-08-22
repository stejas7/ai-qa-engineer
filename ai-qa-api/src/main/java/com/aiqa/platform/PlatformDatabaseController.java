package com.aiqa.platform;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Platform-owner read-only database observability. The API exposes only public application tables and
 * server-sanitized columns. It is intentionally not an arbitrary SQL console.
 */
@RestController
@RequestMapping("/api/platform/database")
public class PlatformDatabaseController {
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Set<String> SENSITIVE_TERMS = Set.of(
            "password", "hash", "secret", "token", "authorization", "cookie", "session",
            "credential", "private_key", "client_secret", "api_key", "encrypted");
    private final JdbcTemplate jdbc;

    public PlatformDatabaseController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/tables")
    public List<TableView> tables() {
        List<String> names = jdbc.queryForList("""
                select table_name
                from information_schema.tables
                where table_schema = 'public' and table_type = 'BASE TABLE'
                order by table_name
                """, String.class);
        List<TableView> result = new ArrayList<>();
        for (String table : names) {
            if (!safeIdentifier(table)) continue;
            long rows = jdbc.queryForObject("select count(*) from " + quote(table), Long.class);
            result.add(new TableView(table, rows, safeColumns(table).size()));
        }
        return result;
    }

    @GetMapping("/tables/{table}")
    public PageView rows(@PathVariable String table,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size) {
        validateTable(table);
        int safePage = Math.max(0, page);
        int safeSize = size == 50 || size == 100 ? size : 20;
        List<String> columns = safeColumns(table);
        if (columns.isEmpty()) return new PageView(table, List.of(), List.of(), safePage, safeSize, 0, 0);

        long total = jdbc.queryForObject("select count(*) from " + quote(table), Long.class);
        long pages = total == 0 ? 0 : (total + safeSize - 1) / safeSize;
        int offset = safePage * safeSize;
        String select = columns.stream().map(this::quote).reduce((a,b) -> a + "," + b).orElse("*");
        String sql = "select " + select + " from " + quote(table) + " limit ? offset ?";
        List<Map<String,Object>> raw = jdbc.queryForList(sql, safeSize, offset);
        List<Map<String,Object>> sanitized = raw.stream().map(this::sanitizeRow).toList();
        return new PageView(table, columns, sanitized, safePage, safeSize, total, pages);
    }

    private void validateTable(String table) {
        if (!safeIdentifier(table)) throw new IllegalArgumentException("Invalid table");
        Integer count = jdbc.queryForObject("""
                select count(*) from information_schema.tables
                where table_schema='public' and table_type='BASE TABLE' and table_name=?
                """, Integer.class, table);
        if (count == null || count == 0) throw new IllegalArgumentException("Unknown table");
    }

    private List<String> safeColumns(String table) {
        return jdbc.queryForList("""
                select column_name from information_schema.columns
                where table_schema='public' and table_name=?
                order by ordinal_position
                """, String.class, table).stream().filter(this::safeColumn).toList();
    }

    private boolean safeColumn(String column) {
        if (!safeIdentifier(column)) return false;
        String lower = column.toLowerCase(Locale.ROOT);
        return SENSITIVE_TERMS.stream().noneMatch(lower::contains);
    }

    private boolean safeIdentifier(String value) {
        return value != null && SAFE_IDENTIFIER.matcher(value).matches();
    }

    private String quote(String identifier) {
        if (!safeIdentifier(identifier)) throw new IllegalArgumentException("Invalid identifier");
        return "\"" + identifier + "\"";
    }

    private Map<String,Object> sanitizeRow(Map<String,Object> row) {
        Map<String,Object> safe = new LinkedHashMap<>();
        row.forEach((key,value) -> {
            if (!safeColumn(key)) return;
            if (value instanceof byte[]) safe.put(key, "[binary data]");
            else {
                String text = value == null ? null : String.valueOf(value);
                safe.put(key, text != null && text.length() > 500 ? text.substring(0, 500) + "…" : value);
            }
        });
        return safe;
    }

    public record TableView(String name, long rows, int visibleColumns) {}
    public record PageView(String table, List<String> columns, List<Map<String,Object>> rows,
                           int page, int size, long totalRows, long totalPages) {}
}
