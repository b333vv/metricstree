package org.b333vv.metric.service;

import com.intellij.openapi.progress.ProgressIndicator;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.ProjectElement;

public interface CalculationService {
    DependenciesBuilder getOrBuildDependencies(ProgressIndicator indicator);

    ProjectElement getOrBuildClassAndMethodModel(ProgressIndicator indicator);

    ProjectElement getOrBuildPackageMetricsModel(ProgressIndicator indicator);

    ProjectElement getOrBuildProjectMetricsModel(ProgressIndicator indicator);

    void calculateProjectTree();

    void calculatePieChart();

    void calculateCategoryChart();

    void calculateXyChart();

    void calculateProfileBoxCharts();

    void calculateProfileCategoryChart();

    void calculateProfileHeatMapChart();

    void calculateProfileRadarCharts();

    void calculateMetricTreeMap();

    void calculateProfileTreeMap();

    void calculateProjectMetricsHistoryChart();

    void exportToXml(String fileName);

    void exportClassMetricsToCsv(String fileName);

    void exportMethodMetricsToCsv(String fileName);

    void exportPackageMetricsToCsv(String fileName);

    void calculateClassFitnessFunctions();

    void calculatePackageFitnessFunctions();
}
