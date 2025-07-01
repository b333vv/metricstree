package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.MetricCategoryChartBuilder;
import org.b333vv.metric.builder.ClassesByMetricsValuesCounter;
import org.knowm.xchart.CategoryChart;

public class CategoryChartDataCalculator {
    public CategoryChart calculate(JavaProject javaProject, Project project) {
        ClassesByMetricsValuesCounter distributor = new ClassesByMetricsValuesCounter(project);
        MetricCategoryChartBuilder metricCategoryChartBuilder = new MetricCategoryChartBuilder();
        return metricCategoryChartBuilder.createChart(distributor.classesByMetricsValuesDistribution(javaProject));
    }
}