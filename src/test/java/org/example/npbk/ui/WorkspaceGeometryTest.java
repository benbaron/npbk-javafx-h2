package org.example.npbk.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkspaceGeometryTest
{
    private static final double EPSILON = 0.0001;
    private static final double PADDING = 8.0;
    private static final double NAVIGATION_WIDTH = 260.0;
    private static final double INSPECTOR_WIDTH = 280.0;
    private static final double SEPARATOR_WIDTH = 1.0;

    @Test
    void laysOutAllFiveRegionsContiguouslyWithoutOverlap()
    {
        WorkspaceGeometry.Layout layout = WorkspaceGeometry.calculate(
            900.0,
            700.0,
            PADDING,
            PADDING,
            PADDING,
            PADDING,
            NAVIGATION_WIDTH,
            INSPECTOR_WIDTH,
            SEPARATOR_WIDTH);

        assertRectangle(layout.navigation(), 8.0, 8.0, 260.0, 684.0);
        assertRectangle(layout.leftSeparator(), 268.0, 8.0, 1.0, 684.0);
        assertRectangle(layout.center(), 269.0, 8.0, 342.0, 684.0);
        assertRectangle(layout.rightSeparator(), 611.0, 8.0, 1.0, 684.0);
        assertRectangle(layout.inspector(), 612.0, 8.0, 280.0, 684.0);

        assertEquals(layout.navigation().right(), layout.leftSeparator().x(), EPSILON);
        assertEquals(layout.leftSeparator().right(), layout.center().x(), EPSILON);
        assertEquals(layout.center().right(), layout.rightSeparator().x(), EPSILON);
        assertEquals(layout.rightSeparator().right(), layout.inspector().x(), EPSILON);
        assertEquals(900.0 - PADDING, layout.rightEdge(), EPSILON);
    }

    @Test
    void onlyCenterWidthChangesWhenWindowGetsWider()
    {
        WorkspaceGeometry.Layout narrow = layoutAtWidth(900.0);
        WorkspaceGeometry.Layout wide = layoutAtWidth(1400.0);

        assertEquals(narrow.navigation().width(), wide.navigation().width(), EPSILON);
        assertEquals(narrow.inspector().width(), wide.inspector().width(), EPSILON);
        assertEquals(500.0, wide.center().width() - narrow.center().width(), EPSILON);
        assertEquals(narrow.navigation().x(), wide.navigation().x(), EPSILON);
        assertEquals(1400.0 - PADDING, wide.rightEdge(), EPSILON);
    }

    @Test
    void exactMinimumWidthProducesZeroWidthCenterWithoutOverlap()
    {
        double minimumWidth = WorkspaceGeometry.minimumWidth(
            PADDING,
            PADDING,
            NAVIGATION_WIDTH,
            INSPECTOR_WIDTH,
            SEPARATOR_WIDTH);
        WorkspaceGeometry.Layout layout = layoutAtWidth(minimumWidth);

        assertEquals(0.0, layout.center().width(), EPSILON);
        assertEquals(layout.leftSeparator().right(), layout.center().x(), EPSILON);
        assertEquals(layout.center().right(), layout.rightSeparator().x(), EPSILON);
        assertEquals(minimumWidth - PADDING, layout.rightEdge(), EPSILON);
        assertTrue(layout.navigation().right() <= layout.inspector().x());
    }

    private WorkspaceGeometry.Layout layoutAtWidth(double width)
    {
        return WorkspaceGeometry.calculate(
            width,
            700.0,
            PADDING,
            PADDING,
            PADDING,
            PADDING,
            NAVIGATION_WIDTH,
            INSPECTOR_WIDTH,
            SEPARATOR_WIDTH);
    }

    private void assertRectangle(
        WorkspaceGeometry.Rectangle rectangle,
        double x,
        double y,
        double width,
        double height)
    {
        assertEquals(x, rectangle.x(), EPSILON);
        assertEquals(y, rectangle.y(), EPSILON);
        assertEquals(width, rectangle.width(), EPSILON);
        assertEquals(height, rectangle.height(), EPSILON);
    }
}
