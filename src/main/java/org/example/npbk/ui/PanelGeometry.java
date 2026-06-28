package org.example.npbk.ui;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/** Shared geometry rules applied to every panel installed in the center host. */
public final class PanelGeometry
{
    private PanelGeometry()
    {
    }

    public static void makeResponsive(Node node)
    {
        if (node instanceof Region region)
        {
            region.setMinSize(0, 0);
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
    }
}
