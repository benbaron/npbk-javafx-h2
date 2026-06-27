package org.example.npbk.ui;

/**
 * Pure geometry model for the three-region application workspace.
 *
 * This class deliberately has no JavaFX control dependency so its layout
 * contract can be tested in a headless Maven build.
 */
public final class WorkspaceGeometry
{
    private WorkspaceGeometry()
    {
    }

    public static Layout calculate(
        double totalWidth,
        double totalHeight,
        double leftPadding,
        double topPadding,
        double rightPadding,
        double bottomPadding,
        double navigationWidth,
        double inspectorWidth,
        double separatorWidth)
    {
        requireNonNegative("totalWidth", totalWidth);
        requireNonNegative("totalHeight", totalHeight);
        requireNonNegative("leftPadding", leftPadding);
        requireNonNegative("topPadding", topPadding);
        requireNonNegative("rightPadding", rightPadding);
        requireNonNegative("bottomPadding", bottomPadding);
        requireNonNegative("navigationWidth", navigationWidth);
        requireNonNegative("inspectorWidth", inspectorWidth);
        requireNonNegative("separatorWidth", separatorWidth);

        double innerWidth = Math.max(0.0, totalWidth - leftPadding - rightPadding);
        double innerHeight = Math.max(0.0, totalHeight - topPadding - bottomPadding);
        double fixedWidth = navigationWidth + inspectorWidth + (separatorWidth * 2.0);
        double centerWidth = Math.max(0.0, innerWidth - fixedWidth);

        double navigationX = leftPadding;
        double leftSeparatorX = navigationX + navigationWidth;
        double centerX = leftSeparatorX + separatorWidth;
        double rightSeparatorX = centerX + centerWidth;
        double inspectorX = rightSeparatorX + separatorWidth;

        return new Layout(
            new Rectangle(navigationX, topPadding, navigationWidth, innerHeight),
            new Rectangle(leftSeparatorX, topPadding, separatorWidth, innerHeight),
            new Rectangle(centerX, topPadding, centerWidth, innerHeight),
            new Rectangle(rightSeparatorX, topPadding, separatorWidth, innerHeight),
            new Rectangle(inspectorX, topPadding, inspectorWidth, innerHeight));
    }

    public static double minimumWidth(
        double leftPadding,
        double rightPadding,
        double navigationWidth,
        double inspectorWidth,
        double separatorWidth)
    {
        requireNonNegative("leftPadding", leftPadding);
        requireNonNegative("rightPadding", rightPadding);
        requireNonNegative("navigationWidth", navigationWidth);
        requireNonNegative("inspectorWidth", inspectorWidth);
        requireNonNegative("separatorWidth", separatorWidth);
        return leftPadding + navigationWidth + separatorWidth
            + separatorWidth + inspectorWidth + rightPadding;
    }

    private static void requireNonNegative(String name, double value)
    {
        if (!Double.isFinite(value) || value < 0.0)
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
    }

    public record Layout(
        Rectangle navigation,
        Rectangle leftSeparator,
        Rectangle center,
        Rectangle rightSeparator,
        Rectangle inspector)
    {
        public double rightEdge()
        {
            return inspector.right();
        }
    }

    public record Rectangle(double x, double y, double width, double height)
    {
        public double right()
        {
            return x + width;
        }

        public double bottom()
        {
            return y + height;
        }
    }
}
