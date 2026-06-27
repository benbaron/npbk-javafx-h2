package org.example.npbk.ui;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.example.npbk.db.Database;
import org.example.npbk.model.BankTiming;
import org.example.npbk.model.BudgetTiming;
import org.example.npbk.model.LookupOption;
import org.example.npbk.model.TransactionEditorViewModel;
import org.example.npbk.model.TransactionLineViewModel;
import org.example.npbk.repo.AccountingLookupRepository;
import org.example.npbk.repo.NormalizedTransactionRepository;
import org.example.npbk.service.NormalizedTransactionService;
import org.example.npbk.validation.NormalizedTransactionValidationService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Spreadsheet-style transaction editor backed by normalized transaction headers
 * and balanced transaction lines.
 */
public class TransactionEditorPanel implements AppPanel
{
    private static final LookupOption BLANK_OPTION = new LookupOption(null, "");

    private final BorderPane root = new BorderPane();
    private final NormalizedTransactionService service;
    private final AccountingLookupRepository lookups;

    private final ObservableList<LookupOption> transactionChoices = FXCollections.observableArrayList();
    private final ObservableList<LookupOption> accountChoices = FXCollections.observableArrayList();
    private final ObservableList<LookupOption> fundChoices = FXCollections.observableArrayList();
    private final ObservableList<LookupOption> budgetCategoryChoices = FXCollections.observableArrayList();
    private final ObservableList<LookupOption> bankAccountChoices = FXCollections.observableArrayList();

    private final ComboBox<LookupOption> transactionPicker = new ComboBox<>(transactionChoices);
    private final DatePicker transactionDate = new DatePicker();
    private final DatePicker postingDate = new DatePicker();
    private final TextField referenceNumber = new TextField();
    private final TextField legalName = new TextField();
    private final TextField description = new TextField();
    private final ComboBox<LookupOption> bankAccount = new ComboBox<>(bankAccountChoices);
    private final ComboBox<BankTiming> affectsBank = new ComboBox<>(FXCollections.observableArrayList(BankTiming.values()));
    private final ComboBox<BudgetTiming> affectsBudget = new ComboBox<>(FXCollections.observableArrayList(BudgetTiming.values()));

    private final TableView<TransactionLineViewModel> lineTable = new TableView<>();
    private final Label debitTotal = new Label("$0.00");
    private final Label creditTotal = new Label("$0.00");
    private final Label balanceTotal = new Label("$0.00");
    private final Label status = new Label("Ready.");
    private final TextArea validationArea = new TextArea();

    private TransactionEditorViewModel current;
    private boolean loading;

    public TransactionEditorPanel(Database database)
    {
        this.lookups = new AccountingLookupRepository(database);
        this.service = new NormalizedTransactionService(
            new NormalizedTransactionRepository(database),
            new NormalizedTransactionValidationService());
        build();
        refreshLookups();
        showNewTransaction();
        refreshTransactionChoices();
    }

    @Override
    public String title()
    {
        return "Transaction Editor";
    }

    @Override
    public Node root()
    {
        return root;
    }

    @Override
    public void onRefresh()
    {
        Long selectedId = current == null ? null : current.getId();
        refreshLookups();
        refreshTransactionChoices();
        if (selectedId != null)
            loadTransaction(selectedId);
        else if (current == null)
            showNewTransaction();
    }

    @Override
    public void onSave()
    {
        saveCurrent();
    }

    private void build()
    {
        root.setMinSize(1180, 720);
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        Label heading = new Label("Transaction Editor");
        heading.getStyleClass().add("h1");
        Label note = new Label(
            "Enter one transaction header and two or more balanced accounting lines. "
            + "The displayed names are backed by stable database IDs.");
        note.setWrapText(true);
        note.getStyleClass().add("muted");

        HBox toolbar = buildToolbar();
        GridPane header = buildHeader();
        HBox lineActions = buildLineActions();
        buildLineTable();
        HBox totals = buildTotals();

        validationArea.setEditable(false);
        validationArea.setPrefRowCount(4);
        validationArea.setWrapText(true);
        validationArea.setPromptText("Validation messages appear here.");

        content.getChildren().addAll(
            heading,
            note,
            toolbar,
            header,
            lineActions,
            lineTable,
            totals,
            new Label("Validation"),
            validationArea);
        VBox.setVgrow(lineTable, Priority.ALWAYS);
        root.setCenter(content);
        installHeaderListeners();
        installTableNavigation();
    }

    private HBox buildToolbar()
    {
        Button newButton = new Button("New");
        newButton.setOnAction(e -> showNewTransaction());
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveCurrent());
        Button reloadButton = new Button("Reload");
        reloadButton.setOnAction(e -> {
            if (current != null && current.getId() != null)
                loadTransaction(current.getId());
            else
                showNewTransaction();
        });

        transactionPicker.setPromptText("Open an existing transaction");
        transactionPicker.setPrefWidth(430);
        transactionPicker.setOnAction(e -> {
            if (loading)
                return;
            LookupOption selected = transactionPicker.getValue();
            if (selected != null && selected.isPresent())
                loadTransaction(selected.id());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(8,
            newButton,
            saveButton,
            reloadButton,
            new Label("Open:"),
            transactionPicker,
            spacer,
            status);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        return toolbar;
    }

    private GridPane buildHeader()
    {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.getStyleClass().add("excel-report-grid");
        for (int i = 0; i < 4; i++)
        {
            ColumnConstraints cc = new ColumnConstraints(220, 280, Double.MAX_VALUE);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        addField(grid, "Transaction Date", transactionDate, 0, 0);
        addField(grid, "Posting Date", postingDate, 1, 0);
        addField(grid, "Reference / Check #", referenceNumber, 2, 0);
        addField(grid, "Legal Name / Payee", legalName, 3, 0);
        addField(grid, "Bank Account", bankAccount, 0, 1);
        addField(grid, "Affects Bank", affectsBank, 1, 1);
        addField(grid, "Affects Budget", affectsBudget, 2, 1);
        addField(grid, "Description", description, 3, 1);
        return grid;
    }

    private void addField(GridPane grid, String labelText, Region control, int column, int row)
    {
        Label label = new Label(labelText);
        label.getStyleClass().add("muted");
        control.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(3, label, control);
        grid.add(box, column, row);
    }

    private HBox buildLineActions()
    {
        Label heading = new Label("Accounting Lines");
        heading.getStyleClass().add("h2");
        Button add = new Button("Add Line");
        add.setOnAction(e -> addLine());
        Button remove = new Button("Remove Selected Line");
        remove.setOnAction(e -> removeSelectedLine());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, heading, spacer, add, remove);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private void buildLineTable()
    {
        lineTable.setEditable(true);
        lineTable.getSelectionModel().setCellSelectionEnabled(true);
        lineTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lineTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        lineTable.setPlaceholder(new Label("Add at least two balanced accounting lines."));

        lineTable.getColumns().add(rowNumberColumn());
        lineTable.getColumns().add(lookupColumn("Account", "account", accountChoices, 280));
        lineTable.getColumns().add(lookupColumn("Fund", "fund", fundChoices, 170));
        lineTable.getColumns().add(lookupColumn("Budget Category", "budgetCategory", budgetCategoryChoices, 210));
        lineTable.getColumns().add(moneyColumn("Debit", true));
        lineTable.getColumns().add(moneyColumn("Credit", false));
        lineTable.getColumns().add(memoColumn());

        lineTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TransactionLineViewModel item, boolean empty)
            {
                super.updateItem(item, empty);
                getStyleClass().remove("row-dirty");
                if (!empty && item != null && item.isDirty())
                    getStyleClass().add("row-dirty");
            }
        });
    }

    private TableColumn<TransactionLineViewModel, Number> rowNumberColumn()
    {
        TableColumn<TransactionLineViewModel, Number> column = new TableColumn<>("#");
        column.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(lineTable.getItems().indexOf(cell.getValue()) + 1));
        column.setEditable(false);
        column.setPrefWidth(48);
        return column;
    }

    private TableColumn<TransactionLineViewModel, LookupOption> lookupColumn(
        String title,
        String property,
        ObservableList<LookupOption> choices,
        double width)
    {
        TableColumn<TransactionLineViewModel, LookupOption> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> switch (property) {
            case "account" -> cell.getValue().accountProperty();
            case "fund" -> cell.getValue().fundProperty();
            case "budgetCategory" -> cell.getValue().budgetCategoryProperty();
            default -> throw new IllegalArgumentException(property);
        });
        column.setCellFactory(ComboBoxTableCell.forTableColumn(choices));
        column.setOnEditCommit(event -> {
            switch (property)
            {
                case "account" -> event.getRowValue().setAccount(event.getNewValue());
                case "fund" -> event.getRowValue().setFund(presentOrNull(event.getNewValue()));
                case "budgetCategory" -> event.getRowValue().setBudgetCategory(presentOrNull(event.getNewValue()));
                default -> throw new IllegalArgumentException(property);
            }
            afterEdit();
        });
        column.setPrefWidth(width);
        return column;
    }

    private TableColumn<TransactionLineViewModel, BigDecimal> moneyColumn(String title, boolean debit)
    {
        TableColumn<TransactionLineViewModel, BigDecimal> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> debit
            ? cell.getValue().debitAmountProperty()
            : cell.getValue().creditAmountProperty());
        column.setCellFactory(cell -> EditingCells.moneyCell());
        column.setOnEditCommit(event -> {
            BigDecimal value = event.getNewValue() == null ? BigDecimal.ZERO : event.getNewValue();
            if (debit)
            {
                event.getRowValue().setDebitAmount(value);
                if (value.compareTo(BigDecimal.ZERO) > 0)
                    event.getRowValue().setCreditAmount(BigDecimal.ZERO);
            }
            else
            {
                event.getRowValue().setCreditAmount(value);
                if (value.compareTo(BigDecimal.ZERO) > 0)
                    event.getRowValue().setDebitAmount(BigDecimal.ZERO);
            }
            afterEdit();
        });
        column.setPrefWidth(130);
        return column;
    }

    private TableColumn<TransactionLineViewModel, String> memoColumn()
    {
        TableColumn<TransactionLineViewModel, String> column = new TableColumn<>("Memo");
        column.setCellValueFactory(cell -> cell.getValue().memoProperty());
        column.setCellFactory(cell -> EditingCells.textCell());
        column.setOnEditCommit(event -> {
            event.getRowValue().setMemo(event.getNewValue());
            afterEdit();
        });
        column.setPrefWidth(300);
        return column;
    }

    private HBox buildTotals()
    {
        Label debitLabel = new Label("Total Debits:");
        Label creditLabel = new Label("Total Credits:");
        Label balanceLabel = new Label("Out of Balance:");
        debitTotal.getStyleClass().add("report-currency-cell");
        creditTotal.getStyleClass().add("report-currency-cell");
        balanceTotal.getStyleClass().add("report-currency-cell");
        HBox totals = new HBox(8,
            debitLabel,
            debitTotal,
            new Label("   "),
            creditLabel,
            creditTotal,
            new Label("   "),
            balanceLabel,
            balanceTotal);
        totals.setAlignment(Pos.CENTER_RIGHT);
        return totals;
    }

    private void installHeaderListeners()
    {
        transactionDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setTransactionDate(newValue);
                afterEdit();
            }
        });
        postingDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setPostingDate(newValue);
                afterEdit();
            }
        });
        referenceNumber.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setReferenceNumber(newValue);
                afterEdit();
            }
        });
        legalName.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setLegalName(newValue);
                afterEdit();
            }
        });
        description.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setDescription(newValue);
                afterEdit();
            }
        });
        bankAccount.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setBankAccount(presentOrNull(newValue));
                afterEdit();
            }
        });
        affectsBank.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setAffectsBank(newValue);
                afterEdit();
            }
        });
        affectsBudget.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!loading && current != null)
            {
                current.setAffectsBudget(newValue);
                afterEdit();
            }
        });
    }

    private void installTableNavigation()
    {
        lineTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                move(1, 0);
                event.consume();
            }
            else if (event.getCode() == KeyCode.TAB)
            {
                move(0, event.isShiftDown() ? -1 : 1);
                event.consume();
            }
        });
    }

    private void move(int rowDelta, int columnDelta)
    {
        TablePosition<TransactionLineViewModel, ?> position = lineTable.getFocusModel().getFocusedCell();
        if (position == null || lineTable.getItems().isEmpty())
            return;
        int row = Math.max(0, Math.min(lineTable.getItems().size() - 1, position.getRow() + rowDelta));
        int column = position.getColumn() + columnDelta;
        int direction = columnDelta == 0 ? 1 : columnDelta;
        while (column >= 0 && column < lineTable.getColumns().size()
            && !lineTable.getColumns().get(column).isEditable())
            column += direction;
        column = Math.max(1, Math.min(lineTable.getColumns().size() - 1, column));
        lineTable.getSelectionModel().clearAndSelect(row, lineTable.getColumns().get(column));
        lineTable.getFocusModel().focus(row, lineTable.getColumns().get(column));
        lineTable.edit(row, lineTable.getColumns().get(column));
    }

    private void refreshLookups()
    {
        accountChoices.setAll(lookups.accounts());
        fundChoices.setAll(withBlank(lookups.funds()));
        budgetCategoryChoices.setAll(withBlank(lookups.budgetCategories()));
        bankAccountChoices.setAll(lookups.bankAccounts());
    }

    private List<LookupOption> withBlank(List<LookupOption> values)
    {
        ObservableList<LookupOption> result = FXCollections.observableArrayList(BLANK_OPTION);
        result.addAll(values);
        return result;
    }

    private void refreshTransactionChoices()
    {
        Long currentId = current == null ? null : current.getId();
        loading = true;
        try
        {
            transactionChoices.setAll(service.transactionChoices());
            transactionPicker.setValue(findById(transactionChoices, currentId));
        }
        finally
        {
            loading = false;
        }
    }

    private void showNewTransaction()
    {
        TransactionEditorViewModel model = service.newTransaction();
        if (!bankAccountChoices.isEmpty())
            model.setBankAccount(bankAccountChoices.get(0));
        LookupOption defaultFund = firstPresent(fundChoices);
        if (defaultFund != null)
            model.getLines().forEach(line -> line.setFund(defaultFund));
        model.markClean();
        display(model);
        transactionPicker.getSelectionModel().clearSelection();
        status.setText("New transaction.");
    }

    private void loadTransaction(long transactionId)
    {
        try
        {
            display(service.load(transactionId));
            loading = true;
            transactionPicker.setValue(findById(transactionChoices, transactionId));
            loading = false;
            status.setText("Loaded transaction " + transactionId + ".");
        }
        catch (RuntimeException ex)
        {
            loading = false;
            status.setText("Could not load transaction: " + ex.getMessage());
        }
    }

    private void display(TransactionEditorViewModel model)
    {
        loading = true;
        try
        {
            current = model;
            transactionDate.setValue(model.getTransactionDate());
            postingDate.setValue(model.getPostingDate());
            referenceNumber.setText(model.getReferenceNumber());
            legalName.setText(model.getLegalName());
            description.setText(model.getDescription());
            bankAccount.setValue(model.getBankAccount());
            affectsBank.setValue(model.getAffectsBank());
            affectsBudget.setValue(model.getAffectsBudget());
            lineTable.setItems(model.getLines());
            updateTotals();
            validateCurrent();
            lineTable.refresh();
        }
        finally
        {
            loading = false;
        }
    }

    private void addLine()
    {
        if (current == null)
            return;
        TransactionLineViewModel line = new TransactionLineViewModel();
        LookupOption defaultFund = firstPresent(fundChoices);
        line.setFund(defaultFund);
        current.getLines().add(line);
        lineTable.getSelectionModel().clearAndSelect(
            current.getLines().size() - 1,
            lineTable.getColumns().get(1));
        lineTable.edit(current.getLines().size() - 1, lineTable.getColumns().get(1));
        afterEdit();
    }

    private void removeSelectedLine()
    {
        if (current == null)
            return;
        TransactionLineViewModel selected = lineTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            status.setText("No accounting line selected.");
            return;
        }
        current.getLines().remove(selected);
        afterEdit();
    }

    private void saveCurrent()
    {
        if (current == null)
            return;
        try
        {
            NormalizedTransactionService.SaveResult result = service.save(current);
            showValidation(result.messages());
            if (result.saved())
            {
                status.setText("Saved transaction " + current.getId() + ".");
                refreshTransactionChoices();
                lineTable.refresh();
            }
            else
            {
                status.setText("Not saved: correct the validation errors.");
            }
        }
        catch (RuntimeException ex)
        {
            status.setText("Could not save transaction: " + ex.getMessage());
        }
    }

    private void afterEdit()
    {
        if (loading || current == null)
            return;
        updateTotals();
        validateCurrent();
        lineTable.refresh();
    }

    private void updateTotals()
    {
        if (current == null)
            return;
        debitTotal.setText(money(current.totalDebits()));
        creditTotal.setText(money(current.totalCredits()));
        balanceTotal.setText(money(current.outOfBalance()));
    }

    private void validateCurrent()
    {
        if (current == null)
            return;
        showValidation(service.validate(current));
    }

    private void showValidation(List<String> messages)
    {
        validationArea.setText(messages == null || messages.isEmpty()
            ? "No validation issues."
            : messages.stream().collect(Collectors.joining("\n")));
    }

    private LookupOption presentOrNull(LookupOption option)
    {
        return option == null || !option.isPresent() ? null : option;
    }

    private LookupOption firstPresent(List<LookupOption> options)
    {
        return options.stream().filter(LookupOption::isPresent).findFirst().orElse(null);
    }

    private LookupOption findById(List<LookupOption> options, Long id)
    {
        if (id == null)
            return null;
        return options.stream().filter(option -> id.equals(option.id())).findFirst().orElse(null);
    }

    private String money(BigDecimal value)
    {
        BigDecimal amount = value == null ? BigDecimal.ZERO : value;
        return "$" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
