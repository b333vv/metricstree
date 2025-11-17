package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartBuilder;
import org.knowm.xchart.XYChart;

import java.util.Map;
import java.util.TreeMap;

public class XyChartDataCalculator {
    
    public static class XyChartResult {
        private final XYChart chart;
        private final Map<String, Double> instability;
        private final Map<String, Double> abstractness;
        
        public XyChartResult(XYChart chart, Map<String, Double> instability, Map<String, Double> abstractness) {
            this.chart = chart;
            this.instability = instability;
            this.abstractness = abstractness;
        }
        
        public XYChart getChart() {
            return chart;
        }
        
        public Map<String, Double> getInstability() {
            return instability;
        }
        
        public Map<String, Double> getAbstractness() {
            return abstractness;
        }
    }
    
    public XyChartResult calculate(ProjectElement projectElement, Project project) {
        Map<String, Double> instability = new TreeMap<>();
        Map<String, Double> abstractness = new TreeMap<>();
        ProjectMetricXYChartDataBuilder.build(projectElement, instability, abstractness);

        ProjectMetricXYChartBuilder projectMetricXYChartBuilder = new ProjectMetricXYChartBuilder(project);
        XYChart chart = projectMetricXYChartBuilder.createChart(instability, abstractness);
        
        return new XyChartResult(chart, instability, abstractness);
    }
}
