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
import org.b333vv.metric.model.util.CommonUtils;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.HeatMapSeries;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileHeatMapChartBuilder {
    private Map<MetricProfile, Set<JavaClass>> classesByMetricProfile;

    public HeatMapChart createChart(Map<MetricProfile, Set<JavaClass>> classesByMetricProfile) {
        this.classesByMetricProfile = classesByMetricProfile;
        HeatMapChart chart = new HeatMapChartBuilder()
                .title("Correlation Between Metric Profiles")
                .width(50)
                .height(50)
                .build();

        Set<MetricProfile> profiles = new TreeSet<>();

        for (Map.Entry<MetricProfile, Set<JavaClass>> profileEntry : classesByMetricProfile.entrySet()) {
            if (profileEntry.getValue() != null && !profileEntry.getValue().isEmpty()) {
                profiles.add(profileEntry.getKey());
            }
        }

        List<Number[]> heatData = new ArrayList<>();

        int i = 0;
        int j = 0;
        for (MetricProfile metricProfile1 : profiles) {
            for (MetricProfile metricProfile2 : profiles) {
                Number[] numbers = {
                        i,
                        j,
                        getHeatData(metricProfile1, metricProfile2)
                };
                heatData.add(numbers);
                j++;
            }
            j = 0;
            i++;
        }

        List<String> xData = profiles.stream()
                .map(MetricProfile::getName)
                .collect(Collectors.toList());
        List<String> yData = profiles.stream()
                .map(MetricProfile::getName)
                .collect(Collectors.toList());

        HeatMapSeries heatMapSeries = chart.addSeries("Classes Normalized Intersection In",
                xData, yData, heatData);

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
        chart.getStyler().setXAxisTicksVisible(false);

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

    private Number getHeatData(MetricProfile profile1, MetricProfile profile2) {
        Set<JavaClass> classesInProfile1 = new HashSet<>(classesByMetricProfile.get(profile1));
        Set<JavaClass> classesInProfile2 = new HashSet<>(classesByMetricProfile.get(profile2));
        int intersectSize = CommonUtils.sizeOfIntersection(classesInProfile1, classesInProfile2);
        classesInProfile1.addAll(classesInProfile2);
        int unionSize = classesInProfile1.size();

        return Math.floor((double) intersectSize / (double) unionSize * 100) / 100;
    }

}
