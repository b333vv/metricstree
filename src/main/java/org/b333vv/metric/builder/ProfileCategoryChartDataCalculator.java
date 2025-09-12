package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.ui.chart.builder.ProfileCategoryChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.knowm.xchart.CategoryChart;

import java.util.Map;
import java.util.Set;

public class ProfileCategoryChartDataCalculator {
    public CategoryChart calculate(Map<FitnessFunction, Set<ClassElement>> classesByProfile) {
        ProfileCategoryChartBuilder profileCategoryChartBuilder = new ProfileCategoryChartBuilder();
        return profileCategoryChartBuilder.createChart(classesByProfile);
    }
}
