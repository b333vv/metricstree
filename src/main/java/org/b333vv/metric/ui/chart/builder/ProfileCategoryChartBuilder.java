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
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ProfileCategoryChartBuilder {

    public CategoryChart createChart(Map<FitnessFunction, Set<ClassElement>> classesByProfiles) {
        // Check for null input to prevent NullPointerException
        if (classesByProfiles == null) {
            classesByProfiles = new HashMap<>();
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(50)
                .height(50)
                .yAxisTitle("Number Of Classes")
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
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setHasAnnotations(false);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);
        chart.getStyler().setLegendBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setToolTipBackgroundColor(backgroundColor);
        chart.getStyler().setToolTipBorderColor(annotationColor);
        chart.getStyler().setToolTipHighlightColor(backgroundColor);
        chart.getStyler().setToolTipType(Styler.ToolTipType.yLabels);

        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);

        int[] i = new int[1];
        i[0] = 1;
        classesByProfiles.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .forEach(e -> {
                    List<Integer> xData = new ArrayList<>();
                    xData.add(i[0]++);
                    int yData = e.getValue().size();
                    chart.addSeries(e.getKey().name(), xData, Collections.singletonList(yData));
                });

        return chart;
    }
}
