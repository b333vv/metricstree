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
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

import static org.b333vv.metric.model.metric.value.RangeType.*;

public class ProjectMetricXYChartBuilder {

    public XYChart createChart(Map<String, Double> instability, Map<String, Double> abstractness) {

        XYChart chart = new XYChartBuilder()
                .width(50)
                .height(50)
                .xAxisTitle("Instability")
                .yAxisTitle("Abstractness")
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
        chart.getStyler().setPlotGridVerticalLinesVisible(true);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setLegendBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setToolTipBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipBorderColor(annotationColor);
        chart.getStyler().setToolTipHighlightColor(backgroundColor);
        chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);

        chart.getStyler().setMarkerSize(8);
        chart.getStyler().setSeriesMarkers(new Marker[]{new Circle()});

        XYSeries mainSequence = chart.addSeries("Main Sequence", List.of(0.00, 1.00), List.of(1.00, 0.00));
        mainSequence.setMarker(new None());
        mainSequence.setLineColor(new JBColor(new Color(0xf9c784), new Color(0xf9c784)));
        mainSequence.setLineStyle(new BasicStroke(0.5f));
        mainSequence.setLabel("Main Sequence");

        instability.forEach((k, v) -> {
            XYSeries xySeries = chart.addSeries(k, List.of(v), List.of(abstractness.get(k)));
            if (MetricsService.getRangeForMetric(MetricType.D)
                    .getRangeType(Value.of(Math.abs(1.0 - v - abstractness.get(k)))) == RangeType.REGULAR) {
                xySeries.setMarkerColor(new JBColor(new Color(0x499C54), new Color(0x499C54)));
            } else {
                xySeries.setMarkerColor(new JBColor(new Color(0xf24c00), new Color(0xf24c00)));
            }
            xySeries.setShowInLegend(false);
            xySeries.setCustomToolTips(true);
            xySeries.setToolTips(new String[] {k});
        });
        return chart;
    }
}
