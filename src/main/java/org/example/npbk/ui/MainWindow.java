package org.example.npbk.ui;

import org.example.npbk.db.Database;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Top-level shell modeled after the sca-jakarta-h2 panel workspace. */
public class MainWindow extends BorderPane {
    private static final double WORKSPACE_MIN_WIDTH = 1100;
    private static final double WORKSPACE_MIN_HEIGHT = 720;

    private final PanelHost panelHost;
    private final NavigationPane navigationPane;
    private final InspectorPane inspectorPane = new InspectorPane();
    private final Label activePanelLabel = new Label("Active: (none)");

    public MainWindow(Database database) {
        this.panelHost = new PanelHost(database);
        this.navigationPane = new NavigationPane(this::openPanel);

        setTop(buildTopChrome());
        SplitPane split = new SplitPane(navigationPane, buildCenterScrollPane(), inspectorPane);
        split.setDividerPositions(0.22, 0.80);
        BorderPane.setMargin(split, new Insets(8));
        setCenter(split);

        openPanel(AppPanelId.DASHBOARD);
    }

    private ScrollPane buildCenterScrollPane() {
        panelHost.setMinSize(WORKSPACE_MIN_WIDTH, WORKSPACE_MIN_HEIGHT);
        panelHost.setPrefSize(WORKSPACE_MIN_WIDTH, WORKSPACE_MIN_HEIGHT);

        ScrollPane scrollPane = new ScrollPane(panelHost);
        scrollPane.getStyleClass().add("workspace-scroll-pane");
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox buildTopChrome() {
        MenuBar menuBar = new MenuBar(buildFileMenu(), buildViewMenu(), buildReportsMenu(), buildHelpMenu());
        ToolBar toolBar = new ToolBar(
                button("Refresh", panelHost::refreshActive),
                button("Save", panelHost::saveActive),
                activePanelLabel,
                spacer(),
                new Label("NonprofitBookkeeping JavaFX/H2")
        );
        VBox chrome = new VBox(menuBar, toolBar);
        chrome.getStyleClass().add("top-chrome");
        return chrome;
    }

    private Menu buildFileMenu() {
        Menu file = new Menu("File");
        file.getItems().addAll(
                item("Dashboard", () -> openPanel(AppPanelId.DASHBOARD)),
                item("Transaction Editor", () -> openPanel(AppPanelId.TRANSACTION_EDITOR)),
                item("Settings", () -> openPanel(AppPanelId.SETTINGS))
        );
        return file;
    }

    private Menu buildViewMenu() {
        Menu view = new Menu("View");
        view.getItems().addAll(
                item("Workbook Summary", () -> openPanel(AppPanelId.WORKBOOK_SUMMARY)),
                item("Workbook Tables", () -> openPanel(AppPanelId.WORKBOOK_TABLES)),
                item("Supplies", () -> openPanel(AppPanelId.SUPPLIES)),
                item("Chart of Accounts", () -> openPanel(AppPanelId.CHART_OF_ACCOUNTS))
        );
        return view;
    }

    private Menu buildReportsMenu() {
        Menu reports = new Menu("Reports");
        reports.getItems().addAll(
                item("Balance Statement", () -> openPanel(AppPanelId.BALANCE_STMT)),
                item("Income Statement", () -> openPanel(AppPanelId.INCOME_STMT)),
                item("Transactions List", () -> openPanel(AppPanelId.TRANSACTIONS_LIST)),
                item("All Checks & Transfers", () -> openPanel(AppPanelId.ALL_CHECKS_TFRS)),
                item("Fund Transfers", () -> openPanel(AppPanelId.FUND_TRANSFERS))
        );
        return reports;
    }

    private Menu buildHelpMenu() {
        Menu help = new Menu("Help");
        help.getItems().add(item("Help", () -> openPanel(AppPanelId.HELP)));
        return help;
    }

    private MenuItem item(String label, Runnable action) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(e -> action.run());
        return item;
    }

    private Button button(String label, Runnable action) {
        Button button = new Button(label);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Region spacer() {
        Region spacer = new Region();
        spacer.setMinWidth(20);
        spacer.setPrefWidth(20);
        return spacer;
    }

    private void openPanel(AppPanelId id) {
        panelHost.show(id);
        navigationPane.highlight(id);
        activePanelLabel.setText("Active: " + panelHost.activeTitle());
        inspectorPane.showPanel(id, panelHost.activeTitle());
    }
}
