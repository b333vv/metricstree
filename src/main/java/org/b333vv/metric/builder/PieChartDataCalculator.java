package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.ui.chart.distributor.ClassesByMetricsValuesDistributor;

import java.util.List;

public class PieChartDataCalculator {

    public List<MetricPieChartBuilder.PieChartStructure> calculate(JavaProject javaProject, Project project) {
        ClassesByMetricsValuesDistributor distributor = new ClassesByMetricsValuesDistributor();
        javaProject.accept(distributor);

        MetricPieChartBuilder metricPieChartBuilder = new MetricPieChartBuilder(project, distributor.getDistribution());
        return metricPieChartBuilder.getChartStructures();
    }
}