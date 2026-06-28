package org.example.npbk.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Shared geometry rules applied to every panel installed in the center host. */
public final class PanelGeometry
{
    private static final double MAX_GRID_COLUMN_MIN_WIDTH = 120.0;

    private PanelGeometry()
    {
    }

    public static void makeResponsive(Node node)
    {
        makeResponsive(node, true);
    }

    private static void makeResponsive(Node node, boolean root)
    {
        if (node instanceof Region region && (root || node instanceof Pane))
        {
            region.setMinSize(0, 0);
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        if (node instanceof GridPane grid)
            normalizeGridColumns(grid);

        if (node instanceof Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
                makeResponsive(child, false);
        }
    }

    private static void normalizeGridColumns(GridPane grid)
    {
        for (ColumnConstraints constraints : grid.getColumnConstraints())
        {
            if (constraints.getMinWidth() > MAX_GRID_COLUMN_MIN_WIDTH)
                constraints.setMinWidth(MAX_GRID_COLUMN_MIN_WIDTH);
            constraints.setMaxWidth(Double.MAX_VALUE);
            if (constraints.getHgrow() == null || constraints.getHgrow() == Priority.NEVER)
                constraints.setHgrow(Priority.SOMETIMES);
            constraints.setFillWidth(true);
        }
    }
}
