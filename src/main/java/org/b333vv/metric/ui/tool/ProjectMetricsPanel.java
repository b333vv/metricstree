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

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import icons.MetricsIcons;
import org.b333vv.metric.event.ButtonsEventListener;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.ui.info.*;
import org.b333vv.metric.ui.settings.MetricsConfigurable;
import org.b333vv.metric.ui.treemap.builder.MetricTypeColorProvider;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.b333vv.metric.service.UIStateService;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.util.EditorUtils;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.XYStyler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ProjectMetricsPanel extends MetricsTreePanel {
    private static final String SPLIT_PROPORTION_2PANELS = "PROJECT_2PANELS_PROPORTION";
    private static final String SPLIT_PROPORTION_3PANELS = "PROJECT_3PANELS_PROPORTION";
    private PackageMetricsTable packageMetricsTable;
    private JPanel chartPanel;
    private final Map<Integer, JBTabbedPane> rightPanelMap = new HashMap<>();
    private MetricsTrimmedSummaryTable metricsTrimmedSummaryTable;
    private JBPanel<?> leftPanel;
    private BottomPanel treeMapBottomPanel;
    private MetricTreeMap<CodeElement> treeMap;
    private XChartPanel<XYChart> projectMetricsHistoryXyChartPanel;

    private ProjectMetricsPanel(Project project) {
        super(project, "Metrics.ProjectMetricsToolbar", SPLIT_PROPORTION_2PANELS);

        // Re-create toolbar with ModuleSelector
        com.intellij.openapi.actionSystem.ActionManager actionManager = com.intellij.openapi.actionSystem.ActionManager
                .getInstance();
        com.intellij.openapi.actionSystem.DefaultActionGroup originalGroup = (com.intellij.openapi.actionSystem.DefaultActionGroup) actionManager
                .getAction("Metrics.ProjectMetricsToolbar");
        com.intellij.openapi.actionSystem.DefaultActionGroup newGroup = new com.intellij.openapi.actionSystem.DefaultActionGroup();
        newGroup.addAll(originalGroup);

        com.intellij.openapi.actionSystem.ActionToolbar actionToolbar = actionManager
                .createActionToolbar("Metrics Toolbar", newGroup, false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        actionToolbar.setTargetComponent(mainPanel);
        setToolbar(actionToolbar.getComponent());

        MetricsEventListener metricsEventListener = new ProjectMetricsEventListener();
        ButtonsEventListener buttonsEventListener = new PressedButtonsEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
        project.getMessageBus().connect(project).subscribe(ButtonsEventListener.BUTTONS_EVENT_LISTENER_TOPIC,
                buttonsEventListener);
    }

    public static ProjectMetricsPanel newInstance(Project project) {
        return new ProjectMetricsPanel(project);
    }

    @Override
    protected void openInEditor(PsiElement psiElement) {
        EditorUtils.openInEditor(project, psiElement);
    }

    private void createTreeMapUIComponents() {
        treeMapBottomPanel = new BottomPanel();
        leftPanel = new JBPanel<>(new BorderLayout());
        mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.add(treeMapBottomPanel.getPanel(), BorderLayout.NORTH);
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(createSplitter(leftPanel, mainPanel, "PROJECT_TREE_MAP_2"), rightPanel,
                "PROJECT_TREE_MAP_1"));
    }

    private void showResults(Set<MetricType> metricTypes, CategoryChart categoryChart) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_CATEGORY_CHART"));

        chartPanel = new XChartPanel<>(categoryChart);
        mainPanel.add(ScrollPaneFactory.createScrollPane(chartPanel), BorderLayout.CENTER);
        MetricsRangesTable metricsRangesTable = new MetricsRangesTable(metricTypes, project);
        JScrollPane scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricsRangesTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollableTablePanel);
    }

    private void showResults(Map<MetricType, Map<ClassElement, Metric>> classesByMetricTypes,
            List<MetricPieChartBuilder.PieChartStructure> chartList) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_PIE_CHART"));

        JBTabbedPane tabs = new JBTabbedPane();
        for (MetricPieChartBuilder.PieChartStructure chartStructure : chartList) {
            chartPanel = new XChartPanel<>(chartStructure.pieChart());
            tabs.insertTab(chartStructure.metricType().name(), null, chartPanel,
                    chartStructure.metricType().description(), chartList.indexOf(chartStructure));
            Map<ClassElement, Metric> classesByMetric = classesByMetricTypes.get(chartStructure.metricType());
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

    private void showResults(XYChart xyChart) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());

        JTextPane explanation = new JTextPane();
        explanation.setContentType("text/html");
        String explanationText = "<html><body style=\"font-family:'Open Sans', sans-serif;\">By default, each time project-level metrics values are calculated, "
                +
                "they are saved in the file &lt;project folder&gt;/.idea/metrics/&lt;time stamp&gt;.json. " +
                "You can control this behavior in the configuration form (Other Settings Tab) by clicking &nbsp</body></html>";
        explanation.setText(explanationText);
        explanation.setEditable(false);
        JLabel showConfigurationForm = new JLabel("<html><body style=\"font-family:'Open Sans', sans-serif;\">" +
                "<u>here.</u></body></html>");
        showConfigurationForm.setAlignmentY(0.85f);
        showConfigurationForm.setOpaque(false);
        showConfigurationForm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        showConfigurationForm.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    MetricsConfigurable metricsConfigurable = new MetricsConfigurable(project);
                    ShowSettingsUtil.getInstance().editConfigurable(project, metricsConfigurable);
                }
            }
        });

        explanation.insertComponent(showConfigurationForm);

        JScrollPane scrollableMetricDescriptionPanel = ScrollPaneFactory.createScrollPane(
                explanation,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableMetricDescriptionPanel.getVerticalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollableMetricDescriptionPanel);

        ProjectMetricsHistoryBottomPanel projectMetricsHistoryBottomPanel = new ProjectMetricsHistoryBottomPanel(
                project);

        projectMetricsHistoryXyChartPanel = new XChartPanel<>(xyChart);
        mainPanel.add(ScrollPaneFactory.createScrollPane(projectMetricsHistoryXyChartPanel), BorderLayout.CENTER);
        mainPanel.add(projectMetricsHistoryBottomPanel.getPanel(), BorderLayout.SOUTH);

        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_XY_CHART"));
    }

    @NotNull
    private JBTabbedPane getJbTabbedPane(Map<ClassElement, Metric> classesByMetric) {
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
    private JBPanel<?> getJbPanel(Map<ClassElement, Metric> classesByMetric, RangeType rangeType) {
        List<ClassesByRangesTable.ClassByRange> classesByRanges = classesByMetric.entrySet().stream()
                .filter(e -> project.getService(SettingsService.class).getRangeForMetric(e.getValue().getType())
                        .getRangeType(e.getValue().getPsiValue()) == rangeType)
                .map(e -> new ClassesByRangesTable.ClassByRange(e.getKey(),
                        project.getService(SettingsService.class).getRangeForMetric(e.getValue().getType())
                                .getRangeByRangeType(rangeType),
                        e.getValue().getPsiValue()))
                .collect(toList());
        ClassesByRangesTable classesByRangesTable = new ClassesByRangesTable(classesByRanges, project);
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

    private void showResults(@NotNull MetricTreeMap<CodeElement> treeMap, ProjectElement projectElement) {
        createTreeMapUIComponents();

        // Set up TreeMap actions to communicate with the UI
        treeMap.setSelectionChangedAction(text -> treeMapBottomPanel.setData(text));
        treeMap.setClickedAction(javaClass -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectTreeMapCellClicked(javaClass);
        });

        MetricTypeSelectorTable metricTypeSelectorTable = new MetricTypeSelectorTable(projectElement, metricType -> {
            treeMap.setColorProvider(new MetricTypeColorProvider(metricType, project));
            treeMap.updateUI();
            treeMap.refresh();
            treeMapBottomPanel.setData(metricType);
        }, project);
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

        metricsTrimmedSummaryTable = new MetricsTrimmedSummaryTable(project);
        JScrollPane scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricsTrimmedSummaryTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollableTablePanel);
    }

    @Override
    public void update(@NotNull PsiJavaFile file) {
    }

    private class ProjectMetricsEventListener implements MetricsEventListener {

        @Override
        public void buildProjectMetricsTree() {
            buildTreeModel();
        }

        @Override
        public void clearProjectMetricsTree() {
            clear();
            project.getService(UIStateService.class).setProjectTreeActive(false);
        }

        @Override
        public void projectMetricsTreeIsReady(javax.swing.tree.DefaultTreeModel treeModel,
                @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
            SwingUtilities.invokeLater(() -> {
                // Get the projectElement from the module-specific cache
                ProjectElement projectElement = project.getService(CacheService.class)
                        .getProjectMetrics(module);
                if (projectElement == null) {
                    // Fallback to class and methods metrics if project metrics not available
                    projectElement = project.getService(CacheService.class)
                            .getClassAndMethodMetrics(module);
                }

                if (projectElement != null) {
                    metricTreeBuilder = new org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder(projectElement,
                            project);
                    showResults(treeModel);
                    buildProjectMetricsTree();
                    project.getService(UIStateService.class).setProjectTreeActive(true);
                } else {
                    // If still null, log an error and don't proceed
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                            .printInfo("Error: projectElement is null when trying to display metrics tree");
                }
            });
        }

        @Override
        public void classByMetricTreeIsReady(
                @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
            SwingUtilities.invokeLater(() -> {
                showResults(project.getService(CacheService.class).getClassesByMetricTree(module));
            });
        }

        @Override
        public void pieChartIsReady(@org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
            SwingUtilities.invokeLater(() -> {
                Map<MetricType, Map<ClassElement, Metric>> classesByMetricTypes = project.getService(CacheService.class)
                        .getClassesByMetricTypes(module);
                List<MetricPieChartBuilder.PieChartStructure> pieChartList = project.getService(CacheService.class)
                        .getPieChartList(module);
                showResults(classesByMetricTypes, Objects.requireNonNull(pieChartList));
            });
        }

        @Override
        public void categoryChartIsReady(
                @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
            SwingUtilities.invokeLater(() -> {
                Map<MetricType, Map<RangeType, Double>> classesByMetricTypes = project.getService(CacheService.class)
                        .getClassesByMetricTypesForCategoryChart(module);
                CategoryChart categoryChart = project.getService(CacheService.class)
                        .getCategoryChart(module);
                showResults(Objects.requireNonNull(classesByMetricTypes).keySet(), categoryChart);
            });
        }

        @Override
        public void projectMetricsHistoryXyChartIsReady() {
            SwingUtilities.invokeLater(() -> {
                XYChart xyChart = project.getService(CacheService.class)
                        .getUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART);
                showResults(xyChart);
            });
        }

        @Override
        public void metricTreeMapIsReady(
                @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
            SwingUtilities.invokeLater(() -> {
                ProjectElement projectElement = project.getService(CacheService.class)
                        .getClassAndMethodMetrics(module);
                MetricTreeMap<CodeElement> treeMap = project.getService(CacheService.class)
                        .getMetricTreeMap(module);
                if (treeMap != null && projectElement != null) {
                    showResults(treeMap, projectElement);
                }
            });
        }

        @Override
        public void setProjectPanelBottomText(String text) {
            SwingUtilities.invokeLater(() -> {
                treeMapBottomPanel.setData(text);
            });
        }

        @Override
        public void projectTreeMapCellClicked(ClassElement javaClass) {
            SwingUtilities.invokeLater(() -> {
                openInEditor(javaClass.getPsiClass());
                metricsTrimmedSummaryTable.set(javaClass);
            });
        }

        @Override
        public void clearProjectPanel() {
            SwingUtilities.invokeLater(() -> {
                projectPanelClear();
            });
        }
    }

    public class PressedButtonsEventListener implements ButtonsEventListener {

        @Override
        public void plusButtonPressed(JButton plusButton, JButton minusButton) {
            XYStyler styler = projectMetricsHistoryXyChartPanel.getChart().getStyler();
            double current = styler.getPlotContentSize();
            double newCurrent = current + 0.01;
            if (newCurrent < 1.0) {
                minusButton.setEnabled(true);
                styler.setPlotContentSize(newCurrent);
            } else {
                plusButton.setEnabled(false);
                styler.setPlotContentSize(0.99);
            }
            projectMetricsHistoryXyChartPanel.revalidate();
            projectMetricsHistoryXyChartPanel.repaint();
        }

        @Override
        public void minusButtonPressed(JButton plusButton, JButton minusButton) {
            XYStyler styler = projectMetricsHistoryXyChartPanel.getChart().getStyler();
            double current = styler.getPlotContentSize();
            double newCurrent = current - 0.01;
            if (newCurrent > 0.1) {
                plusButton.setEnabled(true);
                styler.setPlotContentSize(newCurrent);
            } else {
                minusButton.setEnabled(false);
                styler.setPlotContentSize(0.1);
            }
            projectMetricsHistoryXyChartPanel.revalidate();
            projectMetricsHistoryXyChartPanel.repaint();
        }
    }

}
