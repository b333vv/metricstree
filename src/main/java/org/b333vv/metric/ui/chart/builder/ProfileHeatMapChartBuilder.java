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
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.settings.profile.MetricProfileSettings;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.HeatMapSeries;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileHeatMapChartBuilder {
    private Map<MetricProfile, Set<JavaClass>> classesByMetricProfile;

    public HeatMapChart createChart(Map<MetricProfile, Set<JavaClass>> classesByMetricProfile) {
        this.classesByMetricProfile = classesByMetricProfile;
        HeatMapChart chart = new HeatMapChartBuilder()
                .title("Correlation Between Invalid Metrics Values And Metric Profiles")
                .width(50)
                .height(50)
                .build();
        BasicMetricsValidRangesSettings basicMetricsValidRangesSettings = MetricsUtils.get(MetricsUtils.getCurrentProject(),
                BasicMetricsValidRangesSettings.class);
        DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings = MetricsUtils.get(MetricsUtils.getCurrentProject(),
                DerivativeMetricsValidRangesSettings.class);
        List<String> xData = new ArrayList<>();
        for (MetricType mt : MetricType.values()) {
            if (mt.level() == MetricLevel.CLASS && (basicMetricsValidRangesSettings.getControlledMetricsList().stream()
                    .anyMatch(stub -> stub.getName().equals(mt.name()))
                    || derivativeMetricsValidRangesSettings.getControlledMetricsList().stream()
                    .anyMatch(stub -> stub.getName().equals(mt.name())))) {
                xData.add(mt.name());
            }
        }
        List<String> yData = new ArrayList<>();
        for (Map.Entry<MetricProfile, Set<JavaClass>> profileEntry : classesByMetricProfile.entrySet()) {
            if (profileEntry.getValue() != null && !profileEntry.getValue().isEmpty()) {
                yData.add(profileEntry.getKey().getName());
            }
        }
        List<Number[]> heatData = new ArrayList<>();
        for (int i = 0; i < xData.size(); i++) {
            for (int j = 0; j < yData.size(); j++) {
                Number[] numbers = {
                        i,
                        j,
                        getHeatData(xData.get(i), yData.get(j))
                };
                heatData.add(numbers);
            }
        }


        HeatMapSeries heatMapSeries = chart.addSeries("Classes With Invalid Metric Values To All Classes For", xData, yData, heatData);


        Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
        Color backgroundColor = UIUtil.getPanelBackground();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setAnnotationsFontColor(annotationColor);

        chart.getStyler().setShowValue(true);

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

        JBColor[] sliceColors = new JBColor[] {
                new JBColor(new Color(0x499C54), new Color(0x499C54)),
                new JBColor(new Color(0xf9c784), new Color(0xf9c784)),
                new JBColor(new Color(0xfc7a1e), new Color(0xfc7a1e)),
                new JBColor(new Color(0xf24c00), new Color(0xf24c00))};

        chart.getStyler().setRangeColors(sliceColors);

        heatMapSeries.setMin(0);
        heatMapSeries.setMax(1);

        return chart;
    }

    private Number getHeatData(String metricName, String profileName) {
        MetricType metricType = MetricType.valueOf(metricName);

        long classesNumber = classesByMetricProfile.entrySet().stream()
                .filter(e -> e.getKey().getName().equals(profileName))
                .findFirst()
                .get().getValue().stream()
                .flatMap(JavaClass::metrics)
                .filter(m -> m.getType() == metricType)
                .count();
        long invalidMetricValueClassesNumber = classesByMetricProfile.entrySet().stream()
                .filter(e -> e.getKey().getName().equals(profileName))
                .findFirst()
                .get().getValue().stream()
                .flatMap(JavaClass::metrics)
                .filter(m -> m.getType() == metricType)
                .filter(metric -> MetricsService.getRangeForMetric(metric.getType())
                        .getRangeType(metric.getValue()) != RangeType.REGULAR)
                .count();

        return Math.floor((double) invalidMetricValueClassesNumber / (double) classesNumber * 100) / 100;
    }

}
