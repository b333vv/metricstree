/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.ui.chart.builder;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings1;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings1;
import org.b333vv.metric.util.MetricsService;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfileRadarChartBuilder {
    private Map<MetricProfile, Set<JavaClass>> classesByMetricProfile;

    public List<RadarChartStructure> createChart(Map<MetricProfile, Set<JavaClass>> classesByMetricProfile, Project project) {

        this.classesByMetricProfile = classesByMetricProfile;

        BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1 = project
                .getService(BasicMetricsValidRangesSettings1.class);
//        BasicMetricsValidRangesSettings basicMetricsValidRangesSettings = MetricsUtils.get(MetricsUtils.getCurrentProject(),
//                BasicMetricsValidRangesSettings.class);
//        DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings = MetricsUtils.get(MetricsUtils.getCurrentProject(),
//                DerivativeMetricsValidRangesSettings.class);
        DerivativeMetricsValidRangesSettings1 derivativeMetricsValidRangesSettings1 = project
                .getService(DerivativeMetricsValidRangesSettings1.class);
        List<MetricType> metrics = new ArrayList<>();
        for (MetricType mt : MetricType.values()) {
            if (mt.level() == MetricLevel.CLASS && (basicMetricsValidRangesSettings1.getControlledMetricsList().stream()
                    .anyMatch(stub -> stub.getName().equals(mt.name()))
                    || derivativeMetricsValidRangesSettings1.getControlledMetricsList().stream()
                    .anyMatch(stub -> stub.getName().equals(mt.name())))) {
                metrics.add(mt);
            }
        }

        List<MetricProfile> profiles = new ArrayList<>();
        for (Map.Entry<MetricProfile, Set<JavaClass>> profileEntry : classesByMetricProfile.entrySet()) {
            if (profileEntry.getValue() != null && !profileEntry.getValue().isEmpty()) {
                profiles.add(profileEntry.getKey());
            }
        }
        List<RadarChartStructure> chartStructures = new ArrayList<>();
        for (MetricProfile profile : profiles) {
            double[] numbers = new double[metrics.size()];
            for (int i = 0; i < metrics.size(); i++) {
                numbers[i] = getData(metrics.get(i), profile);
            }
            RadarChart chart = new RadarChartBuilder()
                    .width(200)
                    .height(200)
                    .build();
            chart.setVariableLabels(metrics.stream().map(MetricType::name).toArray(String[]::new));
            RadarSeries series = chart.addSeries(profile.getName(), numbers);
            series.setLineColor(new JBColor(new Color(0xf9c784), new Color(0xf9c784)));
            Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
            Color backgroundColor = UIUtil.getPanelBackground();
            chart.getStyler().setAnnotationsFontColor(annotationColor);

            chart.getStyler().setChartBackgroundColor(backgroundColor);
            chart.getStyler().setPlotBackgroundColor(backgroundColor);
            chart.getStyler().setPlotBorderVisible(false);
            chart.getStyler().setChartTitleFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            chart.getStyler().setChartFontColor(annotationColor);

            chart.getStyler().setHasAnnotations(true);
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setToolTipsEnabled(true);
            chart.getStyler().setToolTipBackgroundColor(backgroundColor);
            chart.getStyler().setToolTipBorderColor(annotationColor);
            chart.getStyler().setToolTipHighlightColor(backgroundColor);
            chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);

            Set<JavaClass> javaClasses = classesByMetricProfile.get(profile);
            Map<JavaClass, List<Metric>> classSetMap = javaClasses.stream()
                    .collect(Collectors.toMap(Function.identity(), m -> m.metrics()
                            .filter(metric -> shouldInclude(metric, metrics)).collect(Collectors.toList())));

            RadarChartStructure chartStructure = new RadarChartStructure(profile,
                    chart, classSetMap);

            chartStructures.add(chartStructure);
        }
        return chartStructures;
    }

    private boolean shouldInclude(Metric metric, List<MetricType> metrics) {
        for (MetricType metricType : metrics) {
            if (metric.getType() == metricType) {
                return true;
            }
        }
        return false;
    }

    private double getData(MetricType metricType, MetricProfile profile) {
        long classesNumber = classesByMetricProfile.entrySet().stream()
                .filter(e -> e.getKey().equals(profile))
                .findFirst()
                .get().getValue().stream()
                .flatMap(JavaClass::metrics)
                .filter(m -> m.getType() == metricType)
                .count();
        long invalidMetricValueClassesNumber = classesByMetricProfile.entrySet().stream()
                .filter(e -> e.getKey().equals(profile))
                .findFirst()
                .get().getValue().stream()
                .flatMap(JavaClass::metrics)
                .filter(m -> m.getType() == metricType)
                .filter(metric -> MetricsService.getRangeForMetric(metric.getType())
                        .getRangeType(metric.getValue()) != RangeType.REGULAR)
                .count();

        return Math.floor((double) invalidMetricValueClassesNumber / (double) classesNumber * 100) / 100;
    }

    public static class RadarChartStructure {
        private final MetricProfile metricProfile;
        private final RadarChart radarChart;
        private final Map<JavaClass, List<Metric>> classes;

        public RadarChartStructure(MetricProfile metricProfile, RadarChart radarChart, Map<JavaClass, List<Metric>> classes) {
            this.metricProfile = metricProfile;
            this.radarChart = radarChart;
            this.classes = classes;
        }

        public MetricProfile getMetricProfile() {
            return metricProfile;
        }

        public RadarChart getRadarChart() {
            return radarChart;
        }

        public Map<JavaClass, List<Metric>> getClasses() {
            return new HashMap<>(classes);
        }
    }
}
