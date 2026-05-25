package org.example.npbk.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public final class EditingCells {
    private EditingCells() {}

    public static <S> TableCell<S, String> textCell() {
        return new TableCell<>() {
            private final TextField textField = new TextField();
            {
                textField.setOnAction(e -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && isEditing()) {
                        commitEdit(textField.getText());
                    }
                });
            }
            @Override public void startEdit() {
                super.startEdit(); textField.setText(getItem()); setText(null); setGraphic(textField); textField.requestFocus(); textField.selectAll();
            }
            @Override public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem()); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else if (isEditing()) { textField.setText(item); setText(null); setGraphic(textField); }
                else { setText(item); setGraphic(null); }
            }
        };
    }

    public static <S> TableCell<S, BigDecimal> moneyCell() {
        return new TableCell<>() {
            private final TextField textField = new TextField();
            {
                textField.setOnAction(e -> commitFromText());
                textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && isEditing()) {
                        commitFromText();
                    }
                });
            }
            @Override public void startEdit() { super.startEdit(); textField.setText(formatPlain(getItem())); setText(null); setGraphic(textField); textField.requestFocus(); textField.selectAll(); }
            @Override public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(formatMoney(getItem())); }
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else if (isEditing()) { textField.setText(formatPlain(item)); setText(null); setGraphic(textField); }
                else { setText(formatMoney(item)); setGraphic(null); }
            }
            private void commitFromText() {
                try { commitEdit(new BigDecimal(textField.getText().trim().replace("$", "").replace(",", ""))); }
                catch (Exception ex) { cancelEdit(); }
            }
        };
    }

    public static <S> TableCell<S, LocalDate> dateCell() {
        return new TableCell<>() {
            private final DatePicker picker = new DatePicker();
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");
            { picker.setConverter(new StringConverter<>() {
                @Override public String toString(LocalDate object) { return object == null ? "" : fmt.format(object); }
                @Override public LocalDate fromString(String string) { return string == null || string.isBlank() ? null : LocalDate.parse(string, fmt); }
            });
                picker.setOnAction(e -> commitEdit(picker.getValue()));
                picker.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && isEditing()) {
                        commitEdit(picker.getValue());
                    }
                });
            }
            @Override public void startEdit() { super.startEdit(); picker.setValue(getItem()); setText(null); setGraphic(picker); picker.requestFocus(); }
            @Override public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem() == null ? "" : fmt.format(getItem())); }
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else if (isEditing()) { picker.setValue(item); setText(null); setGraphic(picker); }
                else { setText(item == null ? "" : fmt.format(item)); setGraphic(null); }
            }
        };
    }


    public static <S> TableCell<S, Long> longCell() {
        return new TableCell<>() {
            private final TextField textField = new TextField();
            {
                textField.setOnAction(e -> commitFromText());
                textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && isEditing()) {
                        commitFromText();
                    }
                });
            }
            @Override public void startEdit() { super.startEdit(); textField.setText(getItem() == null ? "" : getItem().toString()); setText(null); setGraphic(textField); textField.requestFocus(); textField.selectAll(); }
            @Override public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem() == null ? "" : getItem().toString()); }
            @Override protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else if (isEditing()) { textField.setText(item == null ? "" : item.toString()); setText(null); setGraphic(textField); }
                else { setText(item == null ? "" : item.toString()); setGraphic(null); }
            }
            private void commitFromText() {
                String text = textField.getText();
                try { commitEdit(text == null || text.isBlank() ? null : Long.valueOf(text.trim())); }
                catch (Exception ex) { cancelEdit(); }
            }
        };
    }

    private static String formatPlain(BigDecimal v) { return v == null ? "" : v.toPlainString(); }
    private static String formatMoney(BigDecimal v) { return v == null ? "" : "$" + v.setScale(2, java.math.RoundingMode.HALF_UP); }
}
