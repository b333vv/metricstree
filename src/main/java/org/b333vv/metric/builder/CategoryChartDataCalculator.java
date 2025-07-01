package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.MetricCategoryChartBuilder;
import org.b333vv.metric.ui.chart.distributor.ClassesByMetricsValuesCounter;
import org.knowm.xchart.CategoryChart;

public class CategoryChartDataCalculator {
    public CategoryChart calculate(JavaProject javaProject, Project project) {
        ClassesByMetricsValuesCounter distributor = new ClassesByMetricsValuesCounter();
        javaProject.accept(distributor);

        MetricCategoryChartBuilder metricCategoryChartBuilder = new MetricCategoryChartBuilder(project, distributor.getDistribution());
        return metricCategoryChartBuilder.getChart();
    }
}