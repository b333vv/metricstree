package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.ui.chart.builder.ProfileHeatMapChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.knowm.xchart.HeatMapChart;

import java.util.Map;
import java.util.Set;

public class ProfileHeatMapDataCalculator {
    public HeatMapChart calculate(Map<FitnessFunction, Set<ClassElement>> classesByProfile) {
        ProfileHeatMapChartBuilder profileHeatMapChartBuilder = new ProfileHeatMapChartBuilder();
        return profileHeatMapChartBuilder.createChart(classesByProfile);
    }
}
