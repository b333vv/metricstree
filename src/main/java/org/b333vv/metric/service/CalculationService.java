package org.b333vv.metric.service;

public interface CalculationService {
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
}
