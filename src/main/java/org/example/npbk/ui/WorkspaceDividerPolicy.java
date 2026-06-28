package org.example.npbk.ui;

/**
 * Calculates initial divider positions for the resizable three-pane workspace.
 *
 * The side panes begin near their preferred widths, but shrink proportionally
 * when necessary so the center workspace retains its protected minimum width.
 */
public final class WorkspaceDividerPolicy
{
    private WorkspaceDividerPolicy()
    {
    }

    public static Positions initialPositions(
        double totalWidth,
        double navigationPreferredWidth,
        double inspectorPreferredWidth,
        double centerMinimumWidth)
    {
        requirePositive("totalWidth", totalWidth);
        requireNonNegative("navigationPreferredWidth", navigationPreferredWidth);
        requireNonNegative("inspectorPreferredWidth", inspectorPreferredWidth);
        requireNonNegative("centerMinimumWidth", centerMinimumWidth);

        double sidePreferredTotal = navigationPreferredWidth + inspectorPreferredWidth;
        double sideAvailable = Math.max(0.0, totalWidth - centerMinimumWidth);
        double scale = sidePreferredTotal == 0.0
            ? 0.0
            : Math.min(1.0, sideAvailable / sidePreferredTotal);

        double navigationWidth = navigationPreferredWidth * scale;
        double inspectorWidth = inspectorPreferredWidth * scale;
        double leftPosition = navigationWidth / totalWidth;
        double rightPosition = 1.0 - (inspectorWidth / totalWidth);

        if (rightPosition < leftPosition)
            rightPosition = leftPosition;

        return new Positions(
            clamp(leftPosition),
            clamp(rightPosition),
            navigationWidth,
            Math.max(0.0, totalWidth - navigationWidth - inspectorWidth),
            inspectorWidth);
    }

    private static double clamp(double value)
    {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static void requirePositive(String name, double value)
    {
        if (!Double.isFinite(value) || value <= 0.0)
            throw new IllegalArgumentException(name + " must be a finite positive value");
    }

    private static void requireNonNegative(String name, double value)
    {
        if (!Double.isFinite(value) || value < 0.0)
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
    }

    public record Positions(
        double leftDividerPosition,
        double rightDividerPosition,
        double navigationWidth,
        double centerWidth,
        double inspectorWidth)
    {
    }
}
