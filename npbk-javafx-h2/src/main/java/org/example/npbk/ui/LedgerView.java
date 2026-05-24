package org.example.npbk.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.example.npbk.model.*;
import org.example.npbk.repo.LookupRepository;
import org.example.npbk.service.LedgerRecalculationService;
import org.example.npbk.service.TransactionService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LedgerView {
    private final BorderPane root = new BorderPane();
    private final TableView<LedgerRowViewModel> table = new TableView<>();
    private final ObservableList<LedgerRowViewModel> rows = FXCollections.observableArrayList();
    private final TextArea validationArea = new TextArea();
    private final Label status = new Label("Ready.");
    private final LookupRepository lookups;
    private final TransactionService transactionService;
    private final LedgerRecalculationService recalculationService;

    public LedgerView(LookupRepository lookups, TransactionService transactionService, LedgerRecalculationService recalculationService) {
        this.lookups = lookups;
        this.transactionService = transactionService;
        this.recalculationService = recalculationService;
        build();
        load();
    }

    public Node getRoot() { return root; }

    private void build() {
        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setItems(rows);
        table.getColumns().add(rowNumberColumn());
        table.getColumns().add(dateColumn());
        table.getColumns().add(textColumn("Ref #", "referenceNumber", 90));
        table.getColumns().add(textColumn("Name", "legalName", 180));
        table.getColumns().add(textColumn("Details", "details", 220));
        table.getColumns().add(comboStringColumn("Bank Account", "bankAccount", lookups.bankAccounts(), 130));
        table.getColumns().add(comboEnumColumn("Affects Bank", "affectsBank", FXCollections.observableArrayList(BankTiming.values()), 125));
        table.getColumns().add(comboStringColumn("Budget Category", "budgetCategory", lookups.budgetCategories(), 180));
        table.getColumns().add(comboEnumColumn("Affects Budget", "affectsBudget", FXCollections.observableArrayList(BudgetTiming.values()), 130));
        table.getColumns().add(comboStringColumn("Fund", "fund", lookups.funds(), 130));
        table.getColumns().add(moneyColumn("Amount", "amount", true, 110));
        table.getColumns().add(moneyColumn("Bank Effect", "bankEffect", false, 120));
        table.getColumns().add(moneyColumn("Budget Effect", "budgetEffect", false, 125));
        table.getColumns().add(stateColumn());

        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(LedgerRowViewModel item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-error", "row-dirty");
                if (!empty && item != null) {
                    if (item.getValidationState() == ValidationState.ERROR) getStyleClass().add("row-error");
                    else if (item.isDirty()) getStyleClass().add("row-dirty");
                }
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> showMessages(row));
        installNavigation();

        Button add = new Button("Add Row"); add.setOnAction(e -> addRow());
        Button save = new Button("Save Selected"); save.setOnAction(e -> saveSelected());
        Button recalc = new Button("Recalculate All"); recalc.setOnAction(e -> recalculateAll());
        HBox toolbar = new HBox(8, add, save, recalc, status); toolbar.setPadding(new Insets(8));

        validationArea.setEditable(false); validationArea.setPrefRowCount(5);
        validationArea.setPromptText("Validation messages for the selected row appear here.");
        VBox bottom = new VBox(new Label("Validation"), validationArea); bottom.setPadding(new Insets(8));
        VBox.setVgrow(validationArea, Priority.ALWAYS);
        root.setTop(toolbar); root.setCenter(table); root.setBottom(bottom);
    }

    private TableColumn<LedgerRowViewModel, Number> rowNumberColumn() {
        TableColumn<LedgerRowViewModel, Number> col = new TableColumn<>("#");
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(c.getValue()) + 1));
        col.setPrefWidth(45); col.setEditable(false); return col;
    }

    private TableColumn<LedgerRowViewModel, LocalDate> dateColumn() {
        TableColumn<LedgerRowViewModel, LocalDate> col = new TableColumn<>("Date");
        col.setCellValueFactory(c -> c.getValue().transactionDateProperty());
        col.setCellFactory(c -> EditingCells.dateCell());
        col.setOnEditCommit(e -> { e.getRowValue().setTransactionDate(e.getNewValue()); afterEdit(e.getRowValue()); });
        col.setPrefWidth(115); return col;
    }

    private TableColumn<LedgerRowViewModel, String> textColumn(String title, String prop, int width) {
        TableColumn<LedgerRowViewModel, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> switch (prop) {
            case "referenceNumber" -> c.getValue().referenceNumberProperty();
            case "legalName" -> c.getValue().legalNameProperty();
            case "details" -> c.getValue().detailsProperty();
            default -> throw new IllegalArgumentException(prop);
        });
        col.setCellFactory(c -> EditingCells.textCell());
        col.setOnEditCommit(e -> { setString(e.getRowValue(), prop, e.getNewValue()); afterEdit(e.getRowValue()); });
        col.setPrefWidth(width); return col;
    }

    private TableColumn<LedgerRowViewModel, String> comboStringColumn(String title, String prop, ObservableList<String> choices, int width) {
        TableColumn<LedgerRowViewModel, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> switch (prop) {
            case "bankAccount" -> c.getValue().bankAccountProperty();
            case "budgetCategory" -> c.getValue().budgetCategoryProperty();
            case "fund" -> c.getValue().fundProperty();
            default -> throw new IllegalArgumentException(prop);
        });
        col.setCellFactory(ComboBoxTableCell.forTableColumn(choices));
        col.setOnEditCommit(e -> { setString(e.getRowValue(), prop, e.getNewValue()); afterEdit(e.getRowValue()); });
        col.setPrefWidth(width); return col;
    }

    private <E extends Enum<E>> TableColumn<LedgerRowViewModel, E> comboEnumColumn(String title, String prop, ObservableList<E> choices, int width) {
        TableColumn<LedgerRowViewModel, E> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> {
            Object value = prop.equals("affectsBank") ? c.getValue().getAffectsBank() : c.getValue().getAffectsBudget();
            @SuppressWarnings("unchecked") E cast = (E) value;
            return new ReadOnlyObjectWrapper<>(cast);
        });
        col.setCellFactory(ComboBoxTableCell.forTableColumn(choices));
        col.setOnEditCommit(e -> { if (prop.equals("affectsBank")) e.getRowValue().setAffectsBank((BankTiming)e.getNewValue()); else e.getRowValue().setAffectsBudget((BudgetTiming)e.getNewValue()); afterEdit(e.getRowValue()); });
        col.setPrefWidth(width); return col;
    }

    private TableColumn<LedgerRowViewModel, BigDecimal> moneyColumn(String title, String prop, boolean editable, int width) {
        TableColumn<LedgerRowViewModel, BigDecimal> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> switch (prop) {
            case "amount" -> c.getValue().amountProperty();
            case "bankEffect" -> c.getValue().bankEffectProperty();
            case "budgetEffect" -> c.getValue().budgetEffectProperty();
            default -> throw new IllegalArgumentException(prop);
        });
        col.setCellFactory(c -> EditingCells.moneyCell());
        col.setEditable(editable);
        if (editable) col.setOnEditCommit(e -> { e.getRowValue().setAmount(e.getNewValue()); afterEdit(e.getRowValue()); });
        col.setPrefWidth(width); return col;
    }

    private TableColumn<LedgerRowViewModel, ValidationState> stateColumn() {
        TableColumn<LedgerRowViewModel, ValidationState> col = new TableColumn<>("State");
        col.setCellValueFactory(c -> c.getValue().validationStateProperty());
        col.setEditable(false); col.setPrefWidth(90); return col;
    }

    private void setString(LedgerRowViewModel row, String prop, String value) {
        switch (prop) {
            case "referenceNumber" -> row.setReferenceNumber(value);
            case "legalName" -> row.setLegalName(value);
            case "details" -> row.setDetails(value);
            case "bankAccount" -> row.setBankAccount(value);
            case "budgetCategory" -> row.setBudgetCategory(value);
            case "fund" -> row.setFund(value);
            default -> throw new IllegalArgumentException(prop);
        }
    }

    private void afterEdit(LedgerRowViewModel row) { recalculationService.recalculate(row); table.refresh(); showMessages(row); }
    private void recalculateAll() { rows.forEach(recalculationService::recalculate); table.refresh(); status.setText("Recalculated " + rows.size() + " row(s)."); }
    private void addRow() { LedgerRowViewModel row = new LedgerRowViewModel(); rows.add(row); table.getSelectionModel().clearAndSelect(rows.size() - 1, table.getColumns().get(1)); table.edit(rows.size() - 1, table.getColumns().get(1)); }
    private void saveSelected() {
        LedgerRowViewModel row = table.getSelectionModel().getSelectedItem();
        if (row == null) { status.setText("No row selected."); return; }
        recalculationService.recalculate(row);
        var result = transactionService.save(row);
        showMessages(row); table.refresh();
        status.setText(result.saved() ? "Saved row." : "Not saved: fix validation errors.");
    }
    private void load() { rows.setAll(transactionService.loadLedgerRows()); if (rows.isEmpty()) addRow(); rows.forEach(recalculationService::recalculate); }
    private void showMessages(LedgerRowViewModel row) {
        if (row == null) { validationArea.clear(); return; }
        validationArea.setText(row.getValidationMessages().isEmpty() ? "No validation issues." : row.getValidationMessages().stream().collect(Collectors.joining("\n")));
    }
    private void installNavigation() {
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) { move(1, 0); e.consume(); }
            else if (e.getCode() == KeyCode.TAB) { move(0, e.isShiftDown() ? -1 : 1); e.consume(); }
        });
    }
    private void move(int rowDelta, int colDelta) {
        TablePosition<LedgerRowViewModel, ?> pos = table.getFocusModel().getFocusedCell();
        if (pos == null) return;
        int row = Math.max(0, Math.min(rows.size() - 1, pos.getRow() + rowDelta));
        int col = pos.getColumn() + colDelta;
        while (col >= 0 && col < table.getColumns().size() && !table.getColumns().get(col).isEditable()) col += colDelta == 0 ? 1 : colDelta;
        col = Math.max(1, Math.min(table.getColumns().size() - 1, col));
        table.getSelectionModel().clearAndSelect(row, table.getColumns().get(col));
        table.getFocusModel().focus(row, table.getColumns().get(col));
        table.edit(row, table.getColumns().get(col));
    }
}
