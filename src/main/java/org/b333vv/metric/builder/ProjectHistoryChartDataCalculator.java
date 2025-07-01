package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.ui.chart.builder.ProjectMetricsHistoryXYChartBuilder;
import org.knowm.xchart.XYChart;

public class ProjectHistoryChartDataCalculator {
    public XYChart calculate(Project project) {
        ProjectMetricsHistoryXYChartBuilder projectMetricsHistoryXYChartBuilder = new ProjectMetricsHistoryXYChartBuilder(project);
        return projectMetricsHistoryXYChartBuilder.getChart();
    }
}
