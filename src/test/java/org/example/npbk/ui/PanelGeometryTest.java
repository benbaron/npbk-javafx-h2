package org.example.npbk.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;

class PanelGeometryTest
{
    @Test
    void removesLegacyFixedMinimumsFromPanelRoots()
    {
        Region region = new Region();
        region.setMinSize(1180.0, 720.0);
        region.setMaxSize(1180.0, 720.0);

        PanelGeometry.makeResponsive(region);

        assertEquals(0.0, region.getMinWidth());
        assertEquals(0.0, region.getMinHeight());
        assertEquals(Double.MAX_VALUE, region.getMaxWidth());
        assertEquals(Double.MAX_VALUE, region.getMaxHeight());
    }

    @Test
    void normalizesNestedLayoutContainersAndOversizedGridColumns()
    {
        GridPane header = new GridPane();
        for (int i = 0; i < 4; i++)
        {
            ColumnConstraints constraints = new ColumnConstraints(220.0, 280.0, 280.0);
            constraints.setHgrow(Priority.NEVER);
            header.getColumnConstraints().add(constraints);
        }

        BorderPane root = new BorderPane(header);
        root.setMinSize(1180.0, 720.0);

        PanelGeometry.makeResponsive(root);

        assertEquals(0.0, root.getMinWidth());
        assertEquals(0.0, header.getMinWidth());
        for (ColumnConstraints constraints : header.getColumnConstraints())
        {
            assertEquals(120.0, constraints.getMinWidth());
            assertEquals(Double.MAX_VALUE, constraints.getMaxWidth());
            assertEquals(Priority.SOMETIMES, constraints.getHgrow());
        }
    }
}
