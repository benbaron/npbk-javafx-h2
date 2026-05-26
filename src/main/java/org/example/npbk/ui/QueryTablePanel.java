package org.example.npbk.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.example.npbk.db.Database;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Read-only dynamic table panel for database views and tables. */
public class QueryTablePanel implements AppPanel {
    private static final double MIN_COLUMN_WIDTH_CHARS = 10;
    private static final double APPROX_CHAR_WIDTH_PX = 8;
    private static final double CELL_PADDING_PX = 24;
    private static final double MIN_READABLE_COLUMN_WIDTH = MIN_COLUMN_WIDTH_CHARS * APPROX_CHAR_WIDTH_PX + CELL_PADDING_PX;

    protected final Database database;
    private final String title;
    private final String sourceName;
    private final BorderPane root = new BorderPane();
    private final TableView<Map<String, Object>> table = new TableView<>();
    private final Label status = new Label("Ready.");

    public QueryTablePanel(Database database, String title, String sourceName) {
        this.database = database;
        this.title = title;
        this.sourceName = sourceName;
        build();
    }

    private void build() {
        Label heading = new Label(title);
        heading.getStyleClass().add("h1");
        Label source = new Label("Source: " + sourceName + " (first 500 rows)");
        source.getStyleClass().add("muted");
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> onRefresh());
        HBox actions = new HBox(8, refresh, status);
        VBox header = new VBox(4, heading, source, actions);
        header.setPadding(new Insets(10));
        root.setTop(header);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No rows yet."));
        root.setCenter(table);
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void onRefresh() {
        loadQuery("SELECT * FROM " + safeIdentifier(sourceName) + " LIMIT 500");
    }

    protected void loadQuery(String sql) {
        table.getItems().clear();
        table.getColumns().clear();
        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            for (int i = 1; i <= count; i++) {
                String columnName = md.getColumnLabel(i);
                TableColumn<Map<String, Object>, String> col = new TableColumn<>(columnName);
                col.setCellValueFactory(data -> new SimpleStringProperty(format(data.getValue().get(columnName))));
                col.setMinWidth(MIN_READABLE_COLUMN_WIDTH);
                col.setPrefWidth(preferredColumnWidth(columnName));
                table.getColumns().add(col);
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= count; i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
            table.setItems(rows);
            status.setText("Loaded " + rows.size() + " row(s).");
            VBox.setVgrow(table, Priority.ALWAYS);
        } catch (SQLException ex) {
            status.setText("Could not load data: " + ex.getMessage());
        }
    }

    private double preferredColumnWidth(String columnName) {
        int chars = columnName == null ? 0 : columnName.length();
        return Math.max(MIN_READABLE_COLUMN_WIDTH, Math.min(260, chars * APPROX_CHAR_WIDTH_PX + CELL_PADDING_PX));
    }

    protected String safeIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Unsafe SQL identifier: " + identifier);
        }
        return identifier;
    }

    protected String format(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}