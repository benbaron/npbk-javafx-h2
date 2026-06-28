package org.example.npbk.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
