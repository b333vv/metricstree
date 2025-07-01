package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfileBoxChartDataCalculator {
    public List<ProfileBoxChartBuilder.BoxChartStructure> calculate(Map<FitnessFunction, Set<JavaClass>> classesByProfile) {
        ProfileBoxChartBuilder profileBoxChartBuilder = new ProfileBoxChartBuilder();
        return profileBoxChartBuilder.createChart(classesByProfile);
    }
}
