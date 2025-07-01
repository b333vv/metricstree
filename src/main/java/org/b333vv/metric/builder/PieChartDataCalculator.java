package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.builder.ClassesByMetricsValuesCounter;

import java.util.List;

public class PieChartDataCalculator {

    public List<MetricPieChartBuilder.PieChartStructure> calculate(JavaProject javaProject, Project project) {
        ClassesByMetricsValuesCounter counter = new ClassesByMetricsValuesCounter(project);
        return new MetricPieChartBuilder().createChart(counter.classesByMetricsValuesDistribution(javaProject));
    }
}