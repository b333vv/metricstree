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
import com.intellij.util.ui.UIUtil;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.knowm.xchart.BoxChart;
import org.knowm.xchart.BoxChartBuilder;
import org.knowm.xchart.style.BoxStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ProfileBoxChartBuilder {

    public List<BoxChartStructure> createChart(Map<FitnessFunction, Set<JavaClass>> classesByMetricProfile) {
        List<BoxChartStructure> boxCharts = new ArrayList<>();
//        Map<FitnessFunction, Set<JavaClass>> classesByMetricProfileWithoutEmptyMetrics = classesByMetricProfile.entrySet().stream()
//                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty() && !entry.getKey().profile().isEmpty())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (MetricType mt : MetricType.values()) {
            if (mt.level() == MetricLevel.CLASS) {
                Map<String, List<Double>> series = new LinkedHashMap<>();
                for (Map.Entry<FitnessFunction, Set<JavaClass>> profileEntry : classesByMetricProfile.entrySet()) {
                    var values = profileEntry.getValue().stream()
                            .flatMap(JavaCode::metrics)
                            .filter(metric -> metric.getType() == mt)
                            .map(metric -> metric.getPsiValue() == null ? 0.0 : metric.getPsiValue().doubleValue())
                            .toList();
                    if (!values.isEmpty()) {
                        series.put(profileEntry.getKey().name(), values);
                    }
                }

                if (series.isEmpty()) {
                    continue;
                }

                BoxChart chart = new BoxChartBuilder()
                        .title("Distribution Of " + '"' + mt.name() + '"' + " Metric Values By Metric Fitness Functions")
                        .width(series.size())
                        .height(50)
                        .build();

                series.forEach(chart::addSeries);


                chart.getStyler().setPlotContentSize(0.97);
                if (series.size() > 10) {
                    chart.getStyler().setPlotContentSize(0.98);
                }
                if (series.size() > 20) {
                    chart.getStyler().setPlotContentSize(0.99);
                }

                chart.getStyler().setBoxplotCalCulationMethod(BoxStyler.BoxplotCalCulationMethod.N_LESS_1_PLUS_1);

                Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
                Color backgroundColor = UIUtil.getPanelBackground();
                chart.getStyler().setLegendVisible(false);
                chart.getStyler().setAnnotationsFontColor(annotationColor);

                chart.getStyler().setChartBackgroundColor(backgroundColor);
                chart.getStyler().setPlotBackgroundColor(backgroundColor);
                chart.getStyler().setPlotBorderVisible(false);
                chart.getStyler().setPlotGridVerticalLinesVisible(false);
                chart.getStyler().setPlotTicksMarksVisible(false);
                chart.getStyler().setChartTitleFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                chart.getStyler().setChartFontColor(annotationColor);

                chart.getStyler().setAxisTickLabelsColor(annotationColor);

                chart.getStyler().setToolTipsEnabled(true);
                chart.getStyler().setToolTipBackgroundColor(backgroundColor);
                chart.getStyler().setToolTipBorderColor(annotationColor);
                chart.getStyler().setToolTipHighlightColor(backgroundColor);
                chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);

                chart.getStyler().setXAxisTicksVisible(false);

                BoxChartStructure boxChartStructure = new BoxChartStructure(mt, chart);
                boxCharts.add(boxChartStructure);
            }
        }
        return boxCharts;
    }

    public static class BoxChartStructure {
        private final MetricType metricType;
        private final BoxChart boxChart;

        public BoxChartStructure(MetricType metricType, BoxChart boxChart) {
            this.metricType = metricType;
            this.boxChart = boxChart;
        }

        public MetricType getMetricType() {
            return metricType;
        }

        public BoxChart getBoxChart() {
            return boxChart;
        }
    }
}
