package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileBoxChartDataCalculator {
    public List<ProfileBoxChartBuilder.BoxChartStructure> calculate(Map<FitnessFunction, Set<ClassElement>> classesByProfile) {
        ProfileBoxChartBuilder profileBoxChartBuilder = new ProfileBoxChartBuilder();
        return profileBoxChartBuilder.createChart(classesByProfile);
    }
}
