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
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.List;
import java.util.*;

import static org.b333vv.metric.model.metric.value.RangeType.*;

public class MetricCategoryChartBuilder {

    public CategoryChart createChart(Map<MetricType, Map<RangeType, Double>> classesByMetricTypes) {

        CategoryChart chart = new CategoryChartBuilder()
                .width(50)
                .height(50)
                .xAxisTitle("Metric Type")
                .yAxisTitle("Relative Count")
                .build();

        Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
        Color backgroundColor = UIUtil.getPanelBackground();

        chart.getStyler().setAnnotationsFontColor(annotationColor);
        chart.getStyler().setPlotContentSize(.9);
        chart.getStyler().setChartFontColor(annotationColor);
        chart.getStyler().setChartBackgroundColor(backgroundColor);
        chart.getStyler().setPlotBackgroundColor(backgroundColor);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setAxisTickLabelsColor(annotationColor);
        chart.getStyler().setAxisTickMarksColor(annotationColor);
        chart.getStyler().setHasAnnotations(false);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setLegendBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setToolTipBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipBorderColor(annotationColor);
        chart.getStyler().setToolTipHighlightColor(backgroundColor);
        chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);

        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);

        JBColor[] sliceColors = new JBColor[] {
                new JBColor(new Color(0x499C54), new Color(0x499C54)),
                new JBColor(new Color(0xf9c784), new Color(0xf9c784)),
                new JBColor(new Color(0xfc7a1e), new Color(0xfc7a1e)),
                new JBColor(new Color(0xf24c00), new Color(0xf24c00))};

        chart.getStyler().setSeriesColors(sliceColors);

        Map<RangeType, Map<MetricType, Double>> chartData = new LinkedHashMap<>();
        chartData.put(REGULAR, new TreeMap<>());
        chartData.put(HIGH, new TreeMap<>());
        chartData.put(VERY_HIGH, new TreeMap<>());
        chartData.put(EXTREME, new TreeMap<>());

        classesByMetricTypes.forEach((k, v) -> {
            chartData.get(REGULAR).put(k, v.getOrDefault(REGULAR, 0.0));
            chartData.get(HIGH).put(k, v.getOrDefault(HIGH, 0.0));
            chartData.get(VERY_HIGH).put(k, v.getOrDefault(VERY_HIGH, 0.0));
            chartData.get(EXTREME).put(k, v.getOrDefault(EXTREME, 0.0));
        });

        chartData.forEach((k, v) -> {
            List<String> xData = new ArrayList<>();
            v.keySet().forEach(metricType -> xData.add(metricType.name()));
            List<Double> yData = new ArrayList<>(v.values());
            chart.addSeries(k.name(), xData, yData);

        });

        return chart;
    }
}
