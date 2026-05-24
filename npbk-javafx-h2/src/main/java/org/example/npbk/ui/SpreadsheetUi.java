package org.example.npbk.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class SpreadsheetUi {
    private SpreadsheetUi() {}

    public static <S> TableColumn<S, Number> rowNumberColumn(TableView<S> table) {
        TableColumn<S, Number> col = new TableColumn<>("#");
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(c.getValue()) + 1));
        col.setPrefWidth(45);
        col.setEditable(false);
        return col;
    }

    public static <S> void installNavigation(TableView<S> table) {
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) { move(table, 1, 0); e.consume(); }
            else if (e.getCode() == KeyCode.TAB) { move(table, 0, e.isShiftDown() ? -1 : 1); e.consume(); }
        });
    }

    private static <S> void move(TableView<S> table, int rowDelta, int colDelta) {
        TablePosition<S, ?> pos = table.getFocusModel().getFocusedCell();
        if (pos == null || table.getItems().isEmpty()) return;
        int row = Math.max(0, Math.min(table.getItems().size() - 1, pos.getRow() + rowDelta));
        int col = pos.getColumn() + colDelta;
        if (colDelta != 0) {
            while (col >= 0 && col < table.getColumns().size() && !table.getColumns().get(col).isEditable()) col += colDelta;
        }
        col = Math.max(1, Math.min(table.getColumns().size() - 1, col));
        table.getSelectionModel().clearAndSelect(row, table.getColumns().get(col));
        table.getFocusModel().focus(row, table.getColumns().get(col));
        if (table.getColumns().get(col).isEditable()) table.edit(row, table.getColumns().get(col));
    }
}
