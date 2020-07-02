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

package org.b333vv.metric.ui.tool;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import icons.MetricsIcons;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartBuilder;
import org.b333vv.metric.ui.info.BottomPanel;
import org.b333vv.metric.ui.info.ClassesByRangesTable;
import org.b333vv.metric.ui.info.MetricsRangesTable;
import org.b333vv.metric.ui.info.PackageMetricsTable;
import org.b333vv.metric.util.CurrentFileController;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.internal.chartpart.Chart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder.PieChartStructure;

public class MetricsChartPanel extends SimpleToolWindowPanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "SPLIT_PROPORTION";

    private JBPanel<?> rightPanel;
    private JPanel mainPanel;
    private JPanel chartPanel;
    private PackageMetricsTable packageMetricsTable;

    private final Project project;
    private Map<Integer, JBTabbedPane> rightPanelMap = new HashMap<>();

    protected final CurrentFileController scope;

    public MetricsChartPanel(Project project) {
        super(false, true);
        this.project = project;
        createUIComponents();
        scope = new CurrentFileController(project);
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.MetricsChartToolbar"), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());
        MetricsEventListener metricsEventListener = new MetricsChartEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    private void createUIComponents() {
        BottomPanel bottomPanel = new BottomPanel();
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(bottomPanel.getPanel(), BorderLayout.SOUTH);
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel));
    }

    private JComponent createSplitter(JComponent c1, JComponent c2) {
        float savedProportion = PropertiesComponent.getInstance(project)
                .getFloat(MetricsChartPanel.SPLIT_PROPORTION_PROPERTY, (float) 0.65);

        final Splitter splitter = new Splitter(false);
        splitter.setFirstComponent(c1);
        splitter.setSecondComponent(c2);
        splitter.setProportion(savedProportion);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.addPropertyChangeListener(Splitter.PROP_PROPORTION,
                evt -> PropertiesComponent.getInstance(project).setValue(MetricsChartPanel.SPLIT_PROPORTION_PROPERTY,
                        Float.toString(splitter.getProportion())));
        return splitter;
    }

    private void showResults(Set<MetricType> metricTypes, CategoryChart categoryChart) {
        chartPanel = new XChartPanel<>(categoryChart);
        mainPanel.add(ScrollPaneFactory.createScrollPane(chartPanel), BorderLayout.CENTER);
        MetricsRangesTable metricsRangesTable = new MetricsRangesTable(metricTypes);
        JScrollPane scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricsRangesTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollableTablePanel);
    }

    private void showResults(Map<MetricType, Map<JavaClass, Metric>> classesByMetricTypes, List<PieChartStructure> chartList) {
        JBTabbedPane tabs = new JBTabbedPane();
        for (PieChartStructure chartStructure : chartList) {
            chartPanel = new XChartPanel<>(chartStructure.getPieChart());
            tabs.insertTab(chartStructure.getMetricType().name(), null, chartPanel,
                    chartStructure.getMetricType().description(), chartList.indexOf(chartStructure));
            Map<JavaClass, Metric> classesByMetric = classesByMetricTypes.get(chartStructure.getMetricType());
            JBTabbedPane classesByRanges = getJbTabbedPane(classesByMetric);
            rightPanelMap.put(chartList.indexOf(chartStructure), classesByRanges);
        }

        tabs.addChangeListener(e -> {
            rightPanel.removeAll();
            rightPanel.add(rightPanelMap.get(tabs.getSelectedIndex()));
            rightPanel.revalidate();
            rightPanel.repaint();
        });
        mainPanel.add(ScrollPaneFactory.createScrollPane(tabs), BorderLayout.CENTER);
        rightPanel.add(rightPanelMap.get(0));
    }

    private void showResults(XYChart xyChart, Map<String, Double> instability, Map<String, Double> abstractness) {
        chartPanel = new XChartPanel<>(xyChart);
        CoordinateListener mouseListener = new CoordinateListener(xyChart);
        chartPanel.addMouseListener(mouseListener);
        mainPanel.add(ScrollPaneFactory.createScrollPane(chartPanel), BorderLayout.CENTER);
        packageMetricsTable = new PackageMetricsTable(instability, abstractness);
        JScrollPane scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                packageMetricsTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollableTablePanel);
    }

    @NotNull
    private JBTabbedPane getJbTabbedPane(Map<JavaClass, Metric> classesByMetric) {
        JBTabbedPane classesByRanges = new JBTabbedPane();
        JBPanel<?> highRangePanel = getJbPanel(classesByMetric, RangeType.HIGH);
        classesByRanges.insertTab("High", MetricsIcons.HIGH_COLOR, highRangePanel,
                "Classes with high metric values", 0);
        JBPanel<?> veryHighRangePanel = getJbPanel(classesByMetric, RangeType.VERY_HIGH);
        classesByRanges.insertTab("Very-high", MetricsIcons.VERY_HIGH_COLOR, veryHighRangePanel,
                "Classes with very-high metric values", 1);
        JBPanel<?> extremeRangePanel = getJbPanel(classesByMetric, RangeType.EXTREME);
        classesByRanges.insertTab("Extreme", MetricsIcons.EXTREME_COLOR, extremeRangePanel,
                "Classes with extreme metric values", 2);
        return classesByRanges;
    }

    @NotNull
    private JBPanel<?> getJbPanel(Map<JavaClass, Metric> classesByMetric, RangeType rangeType) {
        List<ClassesByRangesTable.ClassByRange> classesByRanges = classesByMetric.entrySet().stream()
                .filter(e -> MetricsService.getRangeForMetric(e.getValue().getType())
                        .getRangeType(e.getValue().getValue()) == rangeType)
                .map(e -> new ClassesByRangesTable.ClassByRange(e.getKey(),
                        MetricsService.getRangeForMetric(e.getValue().getType()).getRangeByRangeType(rangeType),
                        e.getValue().getValue()))
                .collect(toList());
        ClassesByRangesTable classesByRangesTable = new ClassesByRangesTable(classesByRanges);
        JBPanel<?> jbPanel = new JBPanel<>(new BorderLayout());
        jbPanel.add(classesByRangesTable.getComponent());
        return jbPanel;
    }

    private void clear() {
        rightPanel.removeAll();
        mainPanel.removeAll();
        updateUI();
        createUIComponents();
    }

    private class MetricsChartEventListener implements MetricsEventListener {

        @Override
        public void metricsChartBuilt(Set<MetricType> metricTypes,
                                      @NotNull CategoryChart categoryChart) {
            showResults(metricTypes, categoryChart);
        }

        @Override
        public void metricsByMetricTypesChartBuilt(@NotNull List<PieChartStructure> chartList,
                                                   Map<MetricType, Map<JavaClass, Metric>> classesByMetricTypes) {
            showResults(classesByMetricTypes, chartList);
        }

        @Override
        public void projectMetricsChartBuilt(@NotNull XYChart xyChart, Map<String, Double> instability,
                                             Map<String, Double> abstractness) {
            showResults(xyChart, instability, abstractness);
        }

        @Override
        public void clearChartsPanel() {
            clear();
        }
    }

    public class CoordinateListener extends MouseAdapter {
        private XYChart chart;
        public CoordinateListener(XYChart chart) {
            this.chart = chart;
        }
        public void mousePressed(MouseEvent e) {
            double chartX = chart.getChartXFromCoordinate(e.getX());
            double chartY = chart.getChartYFromCoordinate(e.getY());
            packageMetricsTable.updateSelection(chartX, chartY);
        }
    }
}
