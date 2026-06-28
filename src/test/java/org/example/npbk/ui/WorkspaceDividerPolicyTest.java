package org.example.npbk.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkspaceDividerPolicyTest
{
    private static final double EPSILON = 0.0001;
    private static final double NAVIGATION_PREFERRED_WIDTH = 260.0;
    private static final double INSPECTOR_PREFERRED_WIDTH = 280.0;
    private static final double CENTER_MINIMUM_WIDTH = 360.0;

    @Test
    void keepsPreferredSideWidthsWhenWindowHasRoom()
    {
        WorkspaceDividerPolicy.Positions positions = positions(1344.0);

        assertEquals(260.0, positions.navigationWidth(), EPSILON);
        assertEquals(804.0, positions.centerWidth(), EPSILON);
        assertEquals(280.0, positions.inspectorWidth(), EPSILON);
        assertEquals(260.0 / 1344.0, positions.leftDividerPosition(), EPSILON);
        assertEquals((1344.0 - 280.0) / 1344.0, positions.rightDividerPosition(), EPSILON);
    }

    @Test
    void protectsCenterWidthAtTypicalNarrowWindow()
    {
        WorkspaceDividerPolicy.Positions positions = positions(900.0);

        assertEquals(260.0, positions.navigationWidth(), EPSILON);
        assertEquals(CENTER_MINIMUM_WIDTH, positions.centerWidth(), EPSILON);
        assertEquals(280.0, positions.inspectorWidth(), EPSILON);
        assertTrue(positions.leftDividerPosition() < positions.rightDividerPosition());
    }

    @Test
    void shrinksBothSidePanesBeforeShrinkingCenter()
    {
        WorkspaceDividerPolicy.Positions positions = positions(700.0);

        assertEquals(CENTER_MINIMUM_WIDTH, positions.centerWidth(), EPSILON);
        assertTrue(positions.navigationWidth() < NAVIGATION_PREFERRED_WIDTH);
        assertTrue(positions.inspectorWidth() < INSPECTOR_PREFERRED_WIDTH);
        assertEquals(
            NAVIGATION_PREFERRED_WIDTH / INSPECTOR_PREFERRED_WIDTH,
            positions.navigationWidth() / positions.inspectorWidth(),
            EPSILON);
    }

    @Test
    void dividerPositionsAlwaysRemainOrderedAndNormalized()
    {
        for (double width : new double[] { 360.0, 500.0, 700.0, 900.0, 1344.0, 1920.0 })
        {
            WorkspaceDividerPolicy.Positions positions = positions(width);
            assertTrue(positions.leftDividerPosition() >= 0.0);
            assertTrue(positions.rightDividerPosition() <= 1.0);
            assertTrue(positions.leftDividerPosition() <= positions.rightDividerPosition());
            assertEquals(
                width,
                positions.navigationWidth() + positions.centerWidth() + positions.inspectorWidth(),
                EPSILON);
        }
    }

    @Test
    void rejectsInvalidGeometryInputs()
    {
        assertThrows(IllegalArgumentException.class, () -> positions(0.0));
        assertThrows(IllegalArgumentException.class, () ->
            WorkspaceDividerPolicy.initialPositions(900.0, -1.0, 280.0, 360.0));
        assertThrows(IllegalArgumentException.class, () ->
            WorkspaceDividerPolicy.initialPositions(900.0, 260.0, 280.0, Double.NaN));
    }

    private WorkspaceDividerPolicy.Positions positions(double totalWidth)
    {
        return WorkspaceDividerPolicy.initialPositions(
            totalWidth,
            NAVIGATION_PREFERRED_WIDTH,
            INSPECTOR_PREFERRED_WIDTH,
            CENTER_MINIMUM_WIDTH);
    }
}
