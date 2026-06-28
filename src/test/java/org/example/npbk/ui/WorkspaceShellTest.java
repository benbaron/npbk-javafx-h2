package org.example.npbk.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;

class WorkspaceShellTest
{
    private static final double EPSILON = 0.0001;

    @Test
    void appliesNonOverlappingBoundsToActualRegions()
    {
        Region navigation = new Region();
        Region center = new Region();
        Region inspector = new Region();
        WorkspaceShell shell = new WorkspaceShell(
            navigation,
            center,
            inspector,
            260.0,
            280.0,
            1.0,
            8.0);

        shell.resize(900.0, 700.0);
        shell.requestLayout();
        shell.layout();

        assertEquals(8.0, navigation.getLayoutX(), EPSILON);
        assertEquals(260.0, navigation.getWidth(), EPSILON);
        assertEquals(269.0, center.getLayoutX(), EPSILON);
        assertEquals(342.0, center.getWidth(), EPSILON);
        assertEquals(612.0, inspector.getLayoutX(), EPSILON);
        assertEquals(280.0, inspector.getWidth(), EPSILON);

        assertTrue(right(navigation) < center.getLayoutX());
        assertTrue(right(center) < inspector.getLayoutX());
        assertEquals(892.0, right(inspector), EPSILON);
    }

    @Test
    void resizingShellChangesOnlyCenterRegionWidth()
    {
        Region navigation = new Region();
        Region center = new Region();
        Region inspector = new Region();
        WorkspaceShell shell = new WorkspaceShell(
            navigation,
            center,
            inspector,
            260.0,
            280.0,
            1.0,
            8.0);

        shell.resize(900.0, 700.0);
        shell.requestLayout();
        shell.layout();
        double initialCenterWidth = center.getWidth();
        double initialNavigationWidth = navigation.getWidth();
        double initialInspectorWidth = inspector.getWidth();

        shell.resize(1200.0, 700.0);
        shell.requestLayout();
        shell.layout();

        assertEquals(initialNavigationWidth, navigation.getWidth(), EPSILON);
        assertEquals(initialInspectorWidth, inspector.getWidth(), EPSILON);
        assertEquals(initialCenterWidth + 300.0, center.getWidth(), EPSILON);
        assertTrue(right(navigation) < center.getLayoutX());
        assertTrue(right(center) < inspector.getLayoutX());
    }

    @Test
    void minimumWidthKeepsSidePanelsDistinct()
    {
        Region navigation = new Region();
        Region center = new Region();
        Region inspector = new Region();
        WorkspaceShell shell = new WorkspaceShell(
            navigation,
            center,
            inspector,
            260.0,
            280.0,
            1.0,
            8.0);

        double minimumWidth = shell.minWidth(700.0);
        shell.resize(minimumWidth, 700.0);
        shell.requestLayout();
        shell.layout();

        assertEquals(0.0, center.getWidth(), EPSILON);
        assertTrue(right(navigation) < inspector.getLayoutX());
        assertEquals(minimumWidth - 8.0, right(inspector), EPSILON);
    }

    private double right(Region region)
    {
        return region.getLayoutX() + region.getWidth();
    }
}
