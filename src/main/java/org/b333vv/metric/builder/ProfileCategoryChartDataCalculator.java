package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.chart.builder.ProfileCategoryChartBuilder;
import org.b333vv.metric.ui.profile.FitnessFunction;
import org.knowm.xchart.CategoryChart;

import java.util.Map;
import java.util.Set;

public class ProfileCategoryChartDataCalculator {
    public CategoryChart calculate(Map<FitnessFunction, Set<JavaClass>> classesByProfile) {
        ProfileCategoryChartBuilder profileCategoryChartBuilder = new ProfileCategoryChartBuilder(classesByProfile);
        return profileCategoryChartBuilder.getChart();
    }
}
