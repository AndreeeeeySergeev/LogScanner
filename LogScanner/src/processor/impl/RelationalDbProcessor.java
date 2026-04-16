package processor.impl;

import model.LogEvent;
import processor.LogProcessor;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelationalDbProcessor implements LogProcessor {

    @Override
    public List<LogEvent> process(String jdbcUrl, List<String> levels) throws Exception {

        List<LogEvent> events = new ArrayList<>();

        // потом вынесем в конфиг
        String username = "user";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            if (!hasRequiredPrivileges(conn)) {
                throw new SQLException("Недостаточно прав");
            }

            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {

                while (tables.next()) {

                    String tableName = tables.getString("TABLE_NAME");

                    searchTable(conn, tableName, levels, events);
                }
            }

        }

        return events;
    }

    private void searchTable(Connection conn,
                             String tableName,
                             List<String> levels,
                             List<LogEvent> events) throws SQLException {

        List<String> columns = findCandidateColumns(conn, tableName);

        for (String column : columns) {
            searchColumn(conn, tableName, column, levels, events);
        }
    }

    private List<String> findCandidateColumns(Connection conn, String tableName) throws SQLException {

        List<String> columns = new ArrayList<>();
        List<String> keywords = Arrays.asList("level", "log", "severity", "status", "type");

        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {

            while (rs.next()) {

                String columnName = rs.getString("COLUMN_NAME").toLowerCase();

                if (keywords.stream().anyMatch(columnName::contains)) {
                    columns.add(columnName);
                }
            }
        }

        return columns;
    }

    private void searchColumn(Connection conn,
                              String tableName,
                              String column,
                              List<String> levels,
                              List<LogEvent> events) throws SQLException {

        String dbProduct = conn.getMetaData().getDatabaseProductName().toLowerCase();

        String limitClause;

        if (dbProduct.contains("oracle")) {
            limitClause = " FETCH FIRST 1000 ROWS ONLY";
        } else if (dbProduct.contains("sql server")) {
            limitClause = " OFFSET 0 ROWS FETCH NEXT 1000 ROWS ONLY";
        } else {
            limitClause = " LIMIT 1000";
        }

        String whereClause = levels.stream()
                .map(level -> "LOWER(" + column + ") LIKE '%" + level.toLowerCase() + "%'")
                .reduce((a, b) -> a + " OR " + b)
                .orElse("");

        String query = "SELECT * FROM " + tableName +
                " WHERE " + whereClause +
                limitClause;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {

                StringBuilder row = new StringBuilder();

                for (int i = 1; i <= columnCount; i++) {

                    String value = rs.getString(i);

                    if (value != null) {
                        row.append(meta.getColumnName(i))
                                .append(": ")
                                .append(value)
                                .append(" | ");
                    }
                }

                String message = row.toString();

                events.add(new LogEvent(
                        Instant.now(),
                        "REL_DB",
                        "UNKNOWN",
                        message
                ));
            }
        }
    }

    private boolean hasRequiredPrivileges(Connection conn) {

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }
}