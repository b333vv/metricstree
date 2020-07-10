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
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.b333vv.metric.builder.ClassesByMetricsValuesCounter;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.b333vv.metric.model.metric.value.RangeType.*;

public class MetricPieChartBuilder {

    public List<PieChartStructure> createChart(JavaProject javaProject) {
        List<PieChartStructure> pieCharts = new ArrayList<>();

        ClassesByMetricsValuesCounter classesByMetricsValuesCounter = new ClassesByMetricsValuesCounter();
        Map<MetricType, Map<RangeType, Double>> classesByMetricTypes =
                classesByMetricsValuesCounter.classesByMetricsValuesDistribution(javaProject);

        classesByMetricTypes.forEach((k, v) -> {
            PieChart chart = new PieChartBuilder()
                    .title('"' + k.description() + '"' + " Metric Values Distribution")
                    .width(50)
                    .height(50)
                    .build();

            chart.getStyler().setDonutThickness(.5);
            Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
            Color backgroundColor = UIUtil.getPanelBackground();
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
            chart.getStyler().setAnnotationsFontColor(annotationColor);
            chart.getStyler().setAnnotationDistance(1.15);
            chart.getStyler().setPlotContentSize(.9);
            chart.getStyler().setStartAngleInDegrees(90);
            chart.getStyler().setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Donut);
            chart.getStyler().setChartBackgroundColor(backgroundColor);
            chart.getStyler().setPlotBackgroundColor(backgroundColor);
            chart.getStyler().setPlotBorderVisible(false);
            chart.getStyler().setChartTitleFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            chart.getStyler().setChartFontColor(annotationColor);

            JBColor[] sliceColors = new JBColor[] {
                    new JBColor(new Color(0x499C54), new Color(0x499C54)),
                    new JBColor(new Color(0xf9c784), new Color(0xf9c784)),
                    new JBColor(new Color(0xfc7a1e), new Color(0xfc7a1e)),
                    new JBColor(new Color(0xf24c00), new Color(0xf24c00))};

            chart.getStyler().setSeriesColors(sliceColors);

            chart.addSeries("Regular", v.getOrDefault(REGULAR, 0.0));
            chart.addSeries("High", v.getOrDefault(HIGH, 0.0));
            chart.addSeries("Very-high", v.getOrDefault(VERY_HIGH, 0.0));
            chart.addSeries("Extreme", v.getOrDefault(EXTREME, 0.0));

            PieChartStructure pieChartStructure = new PieChartStructure(k, chart);
            pieCharts.add(pieChartStructure);
        });

        return pieCharts;
    }

    public static class PieChartStructure {
        private final MetricType metricType;
        private final PieChart pieChart;

        public PieChartStructure(MetricType metricType, PieChart pieChart) {
            this.metricType = metricType;
            this.pieChart = pieChart;
        }

        public MetricType getMetricType() {
            return metricType;
        }

        public PieChart getPieChart() {
            return pieChart;
        }
    }
}
