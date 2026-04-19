package processor.impl;

import model.LogEvent;
import processor.LogProcessor;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RelationalDbProcessor implements LogProcessor {

    private final String username;
    private final String password;

    public RelationalDbProcessor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void process(String jdbcUrl,
                        String encoding,
                        Consumer<LogEvent> consumer) throws Exception {

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            System.out.println("🔌 Подключение к БД: " + jdbcUrl);

            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {

                while (tables.next()) {

                    String tableName = tables.getString("TABLE_NAME");

                    processTable(conn, tableName, consumer);
                }
            }
        }
    }

    // Таблица
    private void processTable(Connection conn,
                              String tableName,
                              Consumer<LogEvent> consumer) throws SQLException {

        List<String> columns = findCandidateColumns(conn, tableName);

        if (columns.isEmpty()) return;

        for (String column : columns) {
            processColumn(conn, tableName, column, consumer);
        }
    }

    // Поиск колонок
    private List<String> findCandidateColumns(Connection conn,
                                              String tableName) throws SQLException {

        List<String> columns = new ArrayList<>();

        try (ResultSet rs = conn.getMetaData()
                .getColumns(null, null, tableName, null)) {

            while (rs.next()) {

                String columnName = rs.getString("COLUMN_NAME");

                String lower = columnName.toLowerCase();

                if (lower.contains("log") ||
                        lower.contains("message") ||
                        lower.contains("level") ||
                        lower.contains("severity") ||
                        lower.contains("event")) {

                    columns.add(columnName);
                }
            }
        }

        return columns;
    }

    // Чтение данных
    private void processColumn(Connection conn,
                               String tableName,
                               String column,
                               Consumer<LogEvent> consumer) throws SQLException {

        String query = "SELECT " + column + " FROM " + tableName;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {

                String value = rs.getString(1);

                if (value == null || value.isBlank()) continue;

                consumer.accept(new LogEvent(
                        Instant.now(),
                        "REL_DB",
                        "UNKNOWN",
                        value
                ));
            }
        }
    }
}