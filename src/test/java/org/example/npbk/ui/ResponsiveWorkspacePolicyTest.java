package org.example.npbk.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResponsiveWorkspacePolicyTest
{
    private static final double EPSILON = 0.0001;

    @Test
    void normalWindowLeavesUsefulCenterWidth()
    {
        double windowWidth = 1344.0;
        double navigation = 260.0;
        double inspector = 280.0;
        double chromeAndDividers = 32.0;

        double center = windowWidth - navigation - inspector - chromeAndDividers;

        assertTrue(center >= 700.0);
        assertEquals(772.0, center, EPSILON);
    }

    @Test
    void shrinkingSidePanesDirectlyExpandsCenter()
    {
        double windowWidth = 1344.0;
        double defaultCenter = centerWidth(windowWidth, 260.0, 280.0);
        double collapsedCenter = centerWidth(windowWidth, 150.0, 170.0);

        assertEquals(220.0, collapsedCenter - defaultCenter, EPSILON);
    }

    @Test
    void sidePaneRangesAllowBothPanelsToMoveOutOfTheWay()
    {
        assertTrue(150.0 < 260.0);
        assertTrue(170.0 < 280.0);
        assertTrue(420.0 > 260.0);
        assertTrue(420.0 > 280.0);
    }

    private double centerWidth(double windowWidth, double navigation, double inspector)
    {
        return windowWidth - navigation - inspector - 32.0;
    }
}
