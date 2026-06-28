package org.example.npbk.ui.report;

import java.util.ArrayList;
import java.util.List;

/** Pure percentage sizing policy used by semantic table reports. */
final class ReportColumnSizing
{
    private ReportColumnSizing()
    {
    }

    static List<Double> percentages(List<String> labels, List<String> fields)
    {
        if (labels.size() != fields.size())
            throw new IllegalArgumentException("Labels and fields must have the same size");
        if (labels.isEmpty())
            return List.of();

        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;
        for (int index = 0; index < labels.size(); index++)
        {
            String label = labels.get(index) == null ? "" : labels.get(index);
            String field = fields.get(index) == null ? "" : fields.get(index);
            double weight = Math.max(6, Math.min(24, Math.max(label.length(), field.length())));
            weights.add(weight);
            totalWeight += weight;
        }

        List<Double> percentages = new ArrayList<>();
        for (double weight : weights)
            percentages.add((weight / totalWeight) * 100.0);
        return List.copyOf(percentages);
    }
}
