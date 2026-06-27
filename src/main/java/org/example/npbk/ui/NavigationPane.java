package org.example.npbk.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Left navigation anchor modeled after the sca-jakarta-h2 workspace shell. */
public class NavigationPane extends VBox
{
    private final TreeView<NavItem> tree;
    private final Map<AppPanelId, TreeItem<NavItem>> index = new EnumMap<>(AppPanelId.class);
    private final Consumer<AppPanelId> openPanel;

    public NavigationPane(Consumer<AppPanelId> openPanel)
    {
        this.openPanel = openPanel;
        getStyleClass().add("nav");
        setMinSize(0, 0);

        Label title = new Label("Navigation Links");
        title.getStyleClass().add("nav-title");

        TreeItem<NavItem> root = new TreeItem<>(new NavItem(null, "Root"));
        root.setExpanded(true);

        TreeItem<NavItem> operations = group(root, "Operations");
        add(operations, AppPanelId.DASHBOARD, "Dashboard");
        add(operations, AppPanelId.TRANSACTION_EDITOR, "Transaction Editor");
        add(operations, AppPanelId.TRANSACTIONS_LIST, "Transactions List");
        add(operations, AppPanelId.ALL_CHECKS_TFRS, "All Checks & Transfers");
        add(operations, AppPanelId.FUND_TRANSFERS, "Fund Transfers");

        TreeItem<NavItem> workbook = group(root, "Workbook Pages");
        add(workbook, AppPanelId.WORKBOOK_SUMMARY, "Workbook Summary");
        add(workbook, AppPanelId.WORKBOOK_TABLES, "Workbook Tables");
        add(workbook, AppPanelId.SUPPLIES, "Supplies");

        TreeItem<NavItem> reports = group(root, "Reports");
        add(reports, AppPanelId.BALANCE_STMT, "Balance Statement");
        add(reports, AppPanelId.INCOME_STMT, "Income Statement");

        TreeItem<NavItem> reference = group(root, "Reference");
        add(reference, AppPanelId.CHART_OF_ACCOUNTS, "Chart of Accounts");
        add(reference, AppPanelId.FUNDS, "Funds");
        add(reference, AppPanelId.BUDGET_CATEGORIES, "Budget Categories");
        add(reference, AppPanelId.BANK_ACCOUNTS, "Bank Accounts");

        TreeItem<NavItem> workflows = group(root, "Workflows");
        add(workflows, AppPanelId.BANKING, "Banking");
        add(workflows, AppPanelId.PERIOD_CLOSE, "Period Close");

        TreeItem<NavItem> system = group(root, "System");
        add(system, AppPanelId.SETTINGS, "Settings");
        add(system, AppPanelId.HELP, "Help");

        tree = new TreeView<>(root);
        tree.setShowRoot(false);
        tree.setMinSize(0, 0);
        tree.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(tree, Priority.ALWAYS);
        tree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(NavItem item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        tree.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && newSel.getValue() != null && newSel.getValue().panelId() != null)
                openPanel.accept(newSel.getValue().panelId());
        });
        tree.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
            {
                TreeItem<NavItem> selected = tree.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getValue() != null && selected.getValue().panelId() != null)
                    openPanel.accept(selected.getValue().panelId());
            }
        });

        getChildren().addAll(title, tree);
    }

    public void highlight(AppPanelId id)
    {
        TreeItem<NavItem> item = index.get(id);
        if (item != null)
        {
            tree.getSelectionModel().select(item);
            tree.scrollTo(tree.getRow(item));
        }
    }

    private TreeItem<NavItem> group(TreeItem<NavItem> parent, String label)
    {
        TreeItem<NavItem> group = new TreeItem<>(new NavItem(null, label));
        group.setExpanded(true);
        parent.getChildren().add(group);
        return group;
    }

    private void add(TreeItem<NavItem> parent, AppPanelId id, String label)
    {
        TreeItem<NavItem> item = new TreeItem<>(new NavItem(id, label));
        parent.getChildren().add(item);
        index.put(id, item);
    }

    public record NavItem(AppPanelId panelId, String label)
    {
    }
}
