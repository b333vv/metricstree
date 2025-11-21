package org.b333vv.metric.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.ProjectElement;
import org.jetbrains.annotations.Nullable;

public interface CalculationService {
    DependenciesBuilder getOrBuildDependencies(ProgressIndicator indicator, @Nullable Module module);

    ProjectElement getOrBuildClassAndMethodModel(ProgressIndicator indicator, @Nullable Module module);

    ProjectElement getOrBuildPackageMetricsModel(ProgressIndicator indicator, @Nullable Module module);

    ProjectElement getOrBuildProjectMetricsModel(ProgressIndicator indicator, @Nullable Module module);

    void calculateProjectTree(@Nullable Module module);

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

    void calculatePackageFitnessFunctions(@Nullable Module module);
}
