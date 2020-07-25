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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import icons.MetricsIcons;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.ui.info.*;
import org.b333vv.metric.ui.treemap.builder.MetricTypeColorProvider;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.b333vv.metric.util.MetricsUtils.setProjectTreeActive;

public class ProjectMetricsPanel extends MetricsTreePanel {
    private static final String SPLIT_PROPORTION_2PANELS = "PROJECT_2PANELS_PROPORTION";
    private static final String SPLIT_PROPORTION_3PANELS = "PROJECT_3PANELS_PROPORTION";
    private PackageMetricsTable packageMetricsTable;
    private JPanel chartPanel;
    private Map<Integer, JBTabbedPane> rightPanelMap = new HashMap<>();
    private MetricsTrimmedSummaryTable metricsTrimmedSummaryTable;
    private JBPanel<?> leftPanel;
    private BottomPanel treeMapBottomPanel;
    private MetricTreeMap<JavaCode> treeMap;

    private ProjectMetricsPanel(Project project) {
        super(project, "Metrics.ProjectMetricsToolbar", SPLIT_PROPORTION_2PANELS);
        MetricsEventListener metricsEventListener = new ProjectMetricsEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    public static ProjectMetricsPanel newInstance(Project project) {
        return new ProjectMetricsPanel(project);
    }

    @Override
    protected void openInEditor(PsiElement psiElement) {
        MetricsUtils.openInEditor(psiElement);
    }

    private void createTreeMapUIComponents() {
        treeMapBottomPanel = new BottomPanel();
        leftPanel = new JBPanel<>(new BorderLayout());
        mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.add(treeMapBottomPanel.getPanel(), BorderLayout.NORTH);
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(createSplitter(leftPanel, mainPanel, "PROJECT_TREE_MAP_2"), rightPanel, "PROJECT_TREE_MAP_1"));
    }

    private void showResults(Set<MetricType> metricTypes, CategoryChart categoryChart) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_CATEGORY_CHART"));

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

    private void showResults(Map<MetricType, Map<JavaClass, Metric>> classesByMetricTypes, List<MetricPieChartBuilder.PieChartStructure> chartList) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_PIE_CHART"));

        JBTabbedPane tabs = new JBTabbedPane();
        for (MetricPieChartBuilder.PieChartStructure chartStructure : chartList) {
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
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_XY_CHART"));

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

    private void projectPanelClear() {
        if (leftPanel != null) {
            leftPanel.removeAll();
        }
        rightPanel.removeAll();
        mainPanel.removeAll();
        updateUI();
    }

    private void showResults(@NotNull MetricTreeMap<JavaCode> treeMap, JavaProject javaProject) {
        createTreeMapUIComponents();
        MetricTypeSelectorTable metricTypeSelectorTable = new MetricTypeSelectorTable(javaProject, metricType -> {
            treeMap.setColorProvider(new MetricTypeColorProvider(metricType));
            treeMap.updateUI();
            treeMap.refresh();
            treeMapBottomPanel.setData(metricType);
        });
        JScrollPane scrollableMetricTablePanel = ScrollPaneFactory.createScrollPane(
                metricTypeSelectorTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableMetricTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableMetricTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        leftPanel.add(scrollableMetricTablePanel);

        JScrollPane scrollableTreeMapPanel = ScrollPaneFactory.createScrollPane(
                treeMap,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTreeMapPanel.getHorizontalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollableTreeMapPanel);

        metricsTrimmedSummaryTable = new MetricsTrimmedSummaryTable();
        JScrollPane scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricsTrimmedSummaryTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollableTablePanel);
    }

    @Override
    public void update(@NotNull PsiJavaFile file) {}

    private class ProjectMetricsEventListener implements MetricsEventListener {

        @Override
        public void buildProjectMetricsTree() {
            buildTreeModel();
        }

        @Override
        public void clearProjectMetricsTree() {
            clear();
            setProjectTreeActive(false);
        }

        @Override
        public void projectMetricsTreeIsReady() {
            metricTreeBuilder = MetricTaskCache.instance().getUserData(MetricTaskCache.TREE_BUILDER);
            showResults(MetricTaskCache.instance().getUserData(MetricTaskCache.PROJECT_TREE));
            buildProjectMetricsTree();
            setProjectTreeActive(true);
        }

        @Override
        public void classByMetricTreeIsReady() {
            showResults(MetricTaskCache.instance().getUserData(MetricTaskCache.CLASSES_BY_METRIC_TREE));
        }

        @Override
        public void pieChartIsReady() {
            Map<MetricType, Map<JavaClass, Metric>> classesByMetricTypes = MetricTaskCache.instance()
                    .getUserData(MetricTaskCache.CLASSES_BY_METRIC_TYPES);
            List<MetricPieChartBuilder.PieChartStructure> pieChartList = MetricTaskCache.instance()
                    .getUserData(MetricTaskCache.PIE_CHART_LIST);
            showResults(classesByMetricTypes, Objects.requireNonNull(pieChartList));
        }

        @Override
        public void categoryChartIsReady() {
            Map<MetricType, Map<RangeType, Double>> classesByMetricTypes = MetricTaskCache.instance()
                    .getUserData(MetricTaskCache.CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART);
            CategoryChart categoryChart = MetricTaskCache.instance()
                    .getUserData(MetricTaskCache.CATEGORY_CHART);
            showResults(Objects.requireNonNull(classesByMetricTypes).keySet(), categoryChart);
        }

        @Override
        public void xyChartIsReady() {
            Map<String, Double> instability = MetricTaskCache.instance().getUserData(MetricTaskCache.INSTABILITY);
            Map<String, Double> abstractness = MetricTaskCache.instance().getUserData(MetricTaskCache.ABSTRACTNESS);
            XYChart xyChart = MetricTaskCache.instance().getUserData(MetricTaskCache.XY_CHART);
            showResults(xyChart, instability, abstractness);
        }

        @Override
        public void clearProjectPanel() {
            projectPanelClear();
            setProjectTreeActive(false);
        }

        @Override
        public void metricTreeMapIsReady() {
            JavaProject javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.CLASS_AND_METHODS_METRICS);
            MetricTreeMap<JavaCode> treeMap = MetricTaskCache.instance().getUserData(MetricTaskCache.METRIC_TREE_MAP);
            if (treeMap != null && javaProject != null) {
                showResults(treeMap, javaProject);
            }
        }

        @Override
        public void setProjectPanelBottomText(String text) {
            treeMapBottomPanel.setData(text);
        }

        @Override
        public void projectTreeMapCellClicked(JavaClass javaClass) {
            openInEditor(javaClass.getPsiClass());
            metricsTrimmedSummaryTable.set(javaClass);
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
