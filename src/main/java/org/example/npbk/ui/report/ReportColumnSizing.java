package org.example.npbk.ui.report;

/** Pure sizing rules shared by semantic report rendering and geometry tests. */
public final class ReportColumnSizing
{
    private static final double SECTION_MIN_COLUMN_WIDTH = 56;
    private static final double TABLE_MIN_COLUMN_WIDTH = 72;
    private static final double APPROX_CHAR_WIDTH_PX = 7;
    private static final double CELL_PADDING_PX = 20;

    private ReportColumnSizing()
    {
    }

    public static ColumnSize sectionColumn(double preferredWidth)
    {
        requirePositive("preferredWidth", preferredWidth);
        double minimumWidth = Math.max(
            SECTION_MIN_COLUMN_WIDTH,
            Math.min(preferredWidth, preferredWidth * 0.45));
        return new ColumnSize(minimumWidth, preferredWidth);
    }

    public static ColumnSize tableColumn(String label, String field)
    {
        String safeLabel = label == null ? "" : label;
        String safeField = field == null ? "" : field;
        int chars = Math.max(safeLabel.length(), safeField.length());
        double preferredWidth = Math.max(
            96,
            Math.min(220, chars * APPROX_CHAR_WIDTH_PX + CELL_PADDING_PX));
        double minimumWidth = Math.max(
            TABLE_MIN_COLUMN_WIDTH,
            Math.min(preferredWidth, preferredWidth * 0.65));
        return new ColumnSize(minimumWidth, preferredWidth);
    }

    public static double totalMinimumWidth(ColumnSize... columns)
    {
        double total = 0.0;
        if (columns == null)
            return total;
        for (ColumnSize column : columns)
        {
            if (column != null)
                total += column.minimumWidth();
        }
        return total;
    }

    private static void requirePositive(String name, double value)
    {
        if (!Double.isFinite(value) || value <= 0.0)
            throw new IllegalArgumentException(name + " must be a finite positive value");
    }

    public record ColumnSize(double minimumWidth, double preferredWidth)
    {
        public ColumnSize
        {
            requirePositive("minimumWidth", minimumWidth);
            requirePositive("preferredWidth", preferredWidth);
            if (minimumWidth > preferredWidth)
                throw new IllegalArgumentException("minimumWidth cannot exceed preferredWidth");
        }
    }
}
