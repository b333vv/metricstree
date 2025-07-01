package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.chart.builder.ProfileHeatMapChartBuilder;
import org.b333vv.metric.ui.profile.FitnessFunction;
import org.knowm.xchart.HeatMapChart;

import java.util.Map;
import java.util.Set;

public class ProfileHeatMapDataCalculator {
    public HeatMapChart calculate(Map<FitnessFunction, Set<JavaClass>> classesByProfile) {
        ProfileHeatMapChartBuilder profileHeatMapChartBuilder = new ProfileHeatMapChartBuilder(classesByProfile);
        return profileHeatMapChartBuilder.getChart();
    }
}
