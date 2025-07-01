package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.builder.ProjectMetricsSet2Json;
import org.b333vv.metric.ui.chart.builder.ProjectMetricsHistoryXYChartBuilder;
import org.knowm.xchart.XYChart;
import org.json.JSONObject;
import java.util.TreeSet;

public class ProjectHistoryChartDataCalculator {
    public XYChart calculate(Project project) {
        TreeSet<JSONObject> metricsStampSet = ProjectMetricsSet2Json.parseStoredMetricsSnapshots(project);
        ProjectMetricsHistoryXYChartBuilder projectMetricsHistoryXYChartBuilder = new ProjectMetricsHistoryXYChartBuilder();
        return projectMetricsHistoryXYChartBuilder.createChart(metricsStampSet);
    }
}
