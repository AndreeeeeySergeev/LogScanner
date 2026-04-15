package processor.impl;

import processor.LogProcessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class RelationalDbProcessor implements LogProcessor {

    @Override
    public void process(String jdbcUrl,
                        String fileOutput,
                        List<String> levels) throws Exception {

        //потом вынести
        String username = "";
        String password = "";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(fileOutput), StandardCharsets.UTF_8))) {

            if (!hasRequiredPrivileges(conn)) {
                throw new SQLException("Недостаточно прав для выполнения поиска");
            }

            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {

                    String tableName = tables.getString("TABLE_NAME");

                    System.out.println("Обработка таблицы: " + tableName);

                    searchTableForLevels(conn, tableName, writer, levels);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Ошибка работы с БД: " + e.getMessage(), e);
        }
    }

    private boolean hasRequiredPrivileges(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Недостаточно прав: " + e.getMessage());
            return false;
        }
    }

    private void searchTableForLevels(Connection conn,
                                      String tableName,
                                      BufferedWriter writer,
                                      List<String> levels) throws SQLException, IOException {

        List<String> candidateColumns = findCandidateColumns(conn, tableName);

        for (String column : candidateColumns) {
            searchColumnForLevels(conn, tableName, column, writer, levels);
        }
    }

    private List<String> findCandidateColumns(Connection conn,
                                              String tableName) throws SQLException {

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

    private void searchColumnForLevels(Connection conn,
                                       String tableName,
                                       String columnName,
                                       BufferedWriter writer,
                                       List<String> levels)
            throws SQLException, IOException {

        String dbProduct = conn.getMetaData().getDatabaseProductName().toLowerCase();

        String limitClause;

        if (dbProduct.contains("oracle")) {
            limitClause = " FETCH FIRST 1000 ROWS ONLY";
        } else if (dbProduct.contains("microsoft") || dbProduct.contains("sql server")) {
            limitClause = " OFFSET 0 ROWS FETCH NEXT 1000 ROWS ONLY";
        } else {
            limitClause = " LIMIT 1000";
        }

        String whereClause = levels.stream()
                .map(level -> "LOWER(" + columnName + ") LIKE '%" + level.toLowerCase() + "%'")
                .collect(Collectors.joining(" OR "));

        String query = "SELECT * FROM " + tableName +
                " WHERE " + whereClause +
                limitClause;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();

            while (rs.next()) {

                StringBuilder row = new StringBuilder();

                row.append("Таблица: ").append(tableName)
                        .append(", Колонка: ").append(columnName).append(" | ");

                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);

                    if (value != null) {
                        row.append(rsMeta.getColumnName(i))
                                .append(": ")
                                .append(value)
                                .append(" | ");
                    }
                }

                writer.write(row.toString().trim());
                writer.newLine();
            }

        } catch (SQLException e) {
            System.err.println("Ошибка запроса к " + tableName +
                    "." + columnName + ": " + e.getMessage());
        }
    }
}