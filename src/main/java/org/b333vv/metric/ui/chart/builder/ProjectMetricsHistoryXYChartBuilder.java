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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.util.MetricsUtils;
import org.json.JSONObject;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Circle;
import org.knowm.xchart.style.markers.Marker;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ProjectMetricsHistoryXYChartBuilder {
    public XYChart createChart(TreeSet<JSONObject> metricsStampSet) {

        XYChart chart = new XYChartBuilder()
                .width(150)
                .height(150)
                .build();

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setDatePattern("dd.MM.yy HH:mm");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.95);
        Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
        Color backgroundColor = UIUtil.getPanelBackground();

        chart.getStyler().setAnnotationsFontColor(annotationColor);
        chart.getStyler().setPlotContentSize(.6);
        chart.getStyler().setChartFontColor(annotationColor);
        chart.getStyler().setChartBackgroundColor(backgroundColor);
        chart.getStyler().setPlotBackgroundColor(backgroundColor);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setAxisTickLabelsColor(annotationColor);
        chart.getStyler().setAxisTickMarksColor(annotationColor);
        chart.getStyler().setHasAnnotations(false);
        chart.getStyler().setPlotGridVerticalLinesVisible(true);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);
        chart.getStyler().setLegendBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setToolTipBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipBorderColor(annotationColor);
        chart.getStyler().setToolTipHighlightColor(backgroundColor);
        chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);
        chart.getStyler().setMarkerSize(5);
        chart.getStyler().setSeriesMarkers(new Marker[]{new Circle()});

        List<Date> timeStampList = metricsStampSet.stream()
                .map(jsonObject ->
                        new Date(Long.parseLong(jsonObject.getString("time"))))
                .collect(Collectors.toList());

        for (MetricType mt : MetricType.values()) {
            if (mt.level() == MetricLevel.PROJECT) {
                try {
                    chart.addSeries(
                            mt.description(),
                            timeStampList,
                            metricsStampSet.stream()
                                    .map(jsonObject -> Double.valueOf(jsonObject.getString(mt.name()).replace(",", ".")))
                                    .collect(Collectors.toList()));
                } catch (Exception e) {
                    MetricsUtils.getCurrentProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                            .printInfo(e.getClass() + " " + mt.name() + " " + e.getMessage());
//                    MetricsUtils.getConsole().error(e.getClass() + " " + mt.name() + " " + e.getMessage());
                }
            }
        }
        return chart;
    }
}
