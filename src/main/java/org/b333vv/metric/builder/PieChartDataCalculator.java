package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;

import java.util.List;

public class PieChartDataCalculator {

    public List<MetricPieChartBuilder.PieChartStructure> calculate(ProjectElement projectElement, Project project) {
        ClassesByMetricsValuesCounter counter = new ClassesByMetricsValuesCounter(project);
        return new MetricPieChartBuilder().createChart(counter.classesByMetricsValuesDistribution(projectElement));
    }
}