package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartDataBuilder;
import org.knowm.xchart.XYChart;

public class XyChartDataCalculator {
    public XYChart calculate(JavaProject javaProject, Project project) {
        ProjectMetricXYChartDataBuilder projectMetricXYChartDataBuilder = new ProjectMetricXYChartDataBuilder();
        javaProject.accept(projectMetricXYChartDataBuilder);

        ProjectMetricXYChartBuilder projectMetricXYChartBuilder = new ProjectMetricXYChartBuilder(project, projectMetricXYChartDataBuilder.getChartData());
        return projectMetricXYChartBuilder.getChart();
    }
}
