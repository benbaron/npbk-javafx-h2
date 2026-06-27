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
public class MainWindow extends BorderPane
{
    private static final double WORKSPACE_PREF_WIDTH = 1100;
    private static final double WORKSPACE_PREF_HEIGHT = 720;
    private static final double NAVIGATION_MIN_WIDTH = 220;
    private static final double NAVIGATION_PREF_WIDTH = 260;
    private static final double NAVIGATION_MAX_WIDTH = 360;
    private static final double INSPECTOR_MIN_WIDTH = 220;
    private static final double INSPECTOR_PREF_WIDTH = 280;
    private static final double INSPECTOR_MAX_WIDTH = 360;

    private final PanelHost panelHost;
    private final NavigationPane navigationPane;
    private final InspectorPane inspectorPane = new InspectorPane();
    private final Label activePanelLabel = new Label("Active: (none)");

    public MainWindow(Database database)
    {
        this.panelHost = new PanelHost(database);
        this.navigationPane = new NavigationPane(this::openPanel);

        configureSidePanes();
        setTop(buildTopChrome());

        ScrollPane workspace = buildCenterScrollPane();
        SplitPane split = new SplitPane(navigationPane, workspace, inspectorPane);
        split.setMinSize(0, 0);
        SplitPane.setResizableWithParent(navigationPane, false);
        SplitPane.setResizableWithParent(workspace, true);
        SplitPane.setResizableWithParent(inspectorPane, false);
        split.setDividerPositions(0.20, 0.80);
        BorderPane.setMargin(split, new Insets(8));
        setCenter(split);

        openPanel(AppPanelId.DASHBOARD);
    }

    private void configureSidePanes()
    {
        navigationPane.setMinWidth(NAVIGATION_MIN_WIDTH);
        navigationPane.setPrefWidth(NAVIGATION_PREF_WIDTH);
        navigationPane.setMaxWidth(NAVIGATION_MAX_WIDTH);

        inspectorPane.setMinWidth(INSPECTOR_MIN_WIDTH);
        inspectorPane.setPrefWidth(INSPECTOR_PREF_WIDTH);
        inspectorPane.setMaxWidth(INSPECTOR_MAX_WIDTH);
    }

    private ScrollPane buildCenterScrollPane()
    {
        panelHost.setMinSize(WORKSPACE_PREF_WIDTH, WORKSPACE_PREF_HEIGHT);
        panelHost.setPrefSize(WORKSPACE_PREF_WIDTH, WORKSPACE_PREF_HEIGHT);

        ScrollPane scrollPane = new ScrollPane(panelHost);
        scrollPane.getStyleClass().add("workspace-scroll-pane");
        scrollPane.setMinSize(0, 0);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrollPane.setPrefViewportWidth(700);
        scrollPane.setPrefViewportHeight(WORKSPACE_PREF_HEIGHT);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox buildTopChrome()
    {
        MenuBar menuBar = new MenuBar(buildFileMenu(), buildViewMenu(), buildReportsMenu(), buildHelpMenu());
        ToolBar toolBar = new ToolBar(
            button("Refresh", panelHost::refreshActive),
            button("Save", panelHost::saveActive),
            activePanelLabel,
            spacer(),
            new Label("NonprofitBookkeeping JavaFX/H2"));
        VBox chrome = new VBox(menuBar, toolBar);
        chrome.getStyleClass().add("top-chrome");
        return chrome;
    }

    private Menu buildFileMenu()
    {
        Menu file = new Menu("File");
        file.getItems().addAll(
            item("Dashboard", () -> openPanel(AppPanelId.DASHBOARD)),
            item("Transaction Editor", () -> openPanel(AppPanelId.TRANSACTION_EDITOR)),
            item("Settings", () -> openPanel(AppPanelId.SETTINGS)));
        return file;
    }

    private Menu buildViewMenu()
    {
        Menu view = new Menu("View");
        view.getItems().addAll(
            item("Workbook Summary", () -> openPanel(AppPanelId.WORKBOOK_SUMMARY)),
            item("Workbook Tables", () -> openPanel(AppPanelId.WORKBOOK_TABLES)),
            item("Supplies", () -> openPanel(AppPanelId.SUPPLIES)),
            item("Chart of Accounts", () -> openPanel(AppPanelId.CHART_OF_ACCOUNTS)));
        return view;
    }

    private Menu buildReportsMenu()
    {
        Menu reports = new Menu("Reports");
        reports.getItems().addAll(
            item("Balance Statement", () -> openPanel(AppPanelId.BALANCE_STMT)),
            item("Income Statement", () -> openPanel(AppPanelId.INCOME_STMT)),
            item("Transactions List", () -> openPanel(AppPanelId.TRANSACTIONS_LIST)),
            item("All Checks & Transfers", () -> openPanel(AppPanelId.ALL_CHECKS_TFRS)),
            item("Fund Transfers", () -> openPanel(AppPanelId.FUND_TRANSFERS)));
        return reports;
    }

    private Menu buildHelpMenu()
    {
        Menu help = new Menu("Help");
        help.getItems().add(item("Help", () -> openPanel(AppPanelId.HELP)));
        return help;
    }

    private MenuItem item(String label, Runnable action)
    {
        MenuItem item = new MenuItem(label);
        item.setOnAction(e -> action.run());
        return item;
    }

    private Button button(String label, Runnable action)
    {
        Button button = new Button(label);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Region spacer()
    {
        Region spacer = new Region();
        spacer.setMinWidth(20);
        spacer.setPrefWidth(20);
        return spacer;
    }

    private void openPanel(AppPanelId id)
    {
        panelHost.show(id);
        navigationPane.highlight(id);
        activePanelLabel.setText("Active: " + panelHost.activeTitle());
        inspectorPane.showPanel(id, panelHost.activeTitle());
    }
}
