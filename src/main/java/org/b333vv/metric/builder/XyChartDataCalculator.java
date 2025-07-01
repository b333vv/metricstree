package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartBuilder;
import org.b333vv.metric.builder.ProjectMetricXYChartDataBuilder;
import org.knowm.xchart.XYChart;

import java.util.Map;
import java.util.TreeMap;

public class XyChartDataCalculator {
    public XYChart calculate(JavaProject javaProject, Project project) {
        Map<String, Double> instability = new TreeMap<>();
        Map<String, Double> abstractness = new TreeMap<>();
        ProjectMetricXYChartDataBuilder.build(javaProject, instability, abstractness);

        ProjectMetricXYChartBuilder projectMetricXYChartBuilder = new ProjectMetricXYChartBuilder(project);
        return projectMetricXYChartBuilder.createChart(instability, abstractness);
    }
}
