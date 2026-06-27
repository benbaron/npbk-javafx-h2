package org.example.npbk.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.Region;

/**
 * Deterministic three-region shell for navigation, center workspace, and inspector.
 */
public class WorkspaceShell extends Region
{
    private final Region navigation;
    private final Region center;
    private final Region inspector;
    private final Region leftSeparator = new Region();
    private final Region rightSeparator = new Region();
    private final double navigationWidth;
    private final double inspectorWidth;
    private final double separatorWidth;

    public WorkspaceShell(
        Region navigation,
        Region center,
        Region inspector,
        double navigationWidth,
        double inspectorWidth,
        double separatorWidth,
        double padding)
    {
        this.navigation = navigation;
        this.center = center;
        this.inspector = inspector;
        this.navigationWidth = navigationWidth;
        this.inspectorWidth = inspectorWidth;
        this.separatorWidth = separatorWidth;

        setPadding(new Insets(padding));
        getStyleClass().add("workspace-shell");
        leftSeparator.getStyleClass().add("workspace-separator");
        rightSeparator.getStyleClass().add("workspace-separator");
        getChildren().addAll(
            navigation,
            leftSeparator,
            center,
            rightSeparator,
            inspector);
    }

    @Override
    protected void layoutChildren()
    {
        Insets insets = getInsets();
        WorkspaceGeometry.Layout layout = WorkspaceGeometry.calculate(
            getWidth(),
            getHeight(),
            insets.getLeft(),
            insets.getTop(),
            insets.getRight(),
            insets.getBottom(),
            navigationWidth,
            inspectorWidth,
            separatorWidth);

        apply(navigation, layout.navigation());
        apply(leftSeparator, layout.leftSeparator());
        apply(center, layout.center());
        apply(rightSeparator, layout.rightSeparator());
        apply(inspector, layout.inspector());
    }

    @Override
    protected double computeMinWidth(double height)
    {
        Insets insets = getInsets();
        return WorkspaceGeometry.minimumWidth(
            insets.getLeft(),
            insets.getRight(),
            navigationWidth,
            inspectorWidth,
            separatorWidth);
    }

    @Override
    protected double computePrefWidth(double height)
    {
        return computeMinWidth(height) + Math.max(0.0, center.prefWidth(height));
    }

    @Override
    protected double computeMinHeight(double width)
    {
        Insets insets = getInsets();
        double childHeight = Math.max(
            navigation.minHeight(width),
            Math.max(center.minHeight(width), inspector.minHeight(width)));
        return insets.getTop() + childHeight + insets.getBottom();
    }

    @Override
    protected double computePrefHeight(double width)
    {
        Insets insets = getInsets();
        double childHeight = Math.max(
            navigation.prefHeight(width),
            Math.max(center.prefHeight(width), inspector.prefHeight(width)));
        return insets.getTop() + childHeight + insets.getBottom();
    }

    private void apply(Region region, WorkspaceGeometry.Rectangle rectangle)
    {
        region.resizeRelocate(
            rectangle.x(),
            rectangle.y(),
            rectangle.width(),
            rectangle.height());
    }
}
