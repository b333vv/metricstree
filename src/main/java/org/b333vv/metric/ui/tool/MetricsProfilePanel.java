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
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBPanel;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.ui.info.*;
import org.b333vv.metric.ui.profile.ClassesByProfileTable;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.profile.MetricProfileList;
import org.b333vv.metric.ui.settings.profile.MetricProfilePanel;
import org.b333vv.metric.ui.treemap.builder.ProfileColorProvider;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MetricsProfilePanel extends SimpleToolWindowPanel {
    private JBPanel<?> profilesPanel;
    private JBPanel<?> classesPanel;
    private JBPanel<?> metricsPanel;

    private JBPanel<?> rightPanel;
    private JBPanel<?> mainPanel;
    private JBPanel<?> leftPanel;
    private JPanel chartPanel;

    private MetricProfileList metricProfileList;
    private ClassesByProfileTable classesTable;
    private MetricsSummaryTable metricsSummaryTable;

    private final Project project;
    private Map<MetricProfile, Set<JavaClass>> distribution;
    private List<ProfileBoxChartBuilder.BoxChartStructure> boxChartList;
    private List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts;

    private MetricsTrimmedSummaryTable metricsTrimmedSummaryTable;
    private BottomPanel treeMapBottomPanel;
    private MetricTreeMap<JavaCode> treeMap;

    public MetricsProfilePanel(Project project) {
        super(false, true);
        this.project = project;
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.MetricsProfileToolbar"), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());
        MetricsEventListener metricsEventListener = new MetricsChartEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    private void createProfileUIComponents() {
        profilesPanel = new JBPanel<>(new BorderLayout());
        metricProfileList = new MetricProfileList();
        profilesPanel.add(metricProfileList.getComponent());

        classesPanel = new JBPanel<>(new BorderLayout());
        classesTable = new ClassesByProfileTable();
        classesPanel.add(classesTable.getComponent());

        metricsPanel = new JBPanel<>(new BorderLayout());
        metricsSummaryTable = new MetricsSummaryTable(false);
        metricsPanel.add(metricsSummaryTable.getComponent());

        super.setContent(createSplitter(profilesPanel, createSplitter(classesPanel, metricsPanel, "PROFILE_2"),
                "PROFILE_1"));
    }

    private JComponent createSplitter(JComponent c1, JComponent c2, String splitProportionProperty) {
        float savedProportion = PropertiesComponent.getInstance(project)
                .getFloat(splitProportionProperty, (float) 0.35);
        final JBSplitter splitter = new JBSplitter(false);
        splitter.setFirstComponent(c1);
        splitter.setSecondComponent(c2);
        splitter.setProportion(savedProportion);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.addPropertyChangeListener(Splitter.PROP_PROPORTION,
                evt -> PropertiesComponent.getInstance(project).setValue(splitProportionProperty,
                        Float.toString(splitter.getProportion())));
        return splitter;
    }

    public void setDistribution(Map<MetricProfile, Set<JavaClass>> distribution) {
        this.distribution = distribution;
    }

    private void showProfiles() {
        metricProfileList.setProfiles(new TreeMap<>(distribution));
    }

    private void showClasses(MetricProfile profile) {
        classesTable.setClasses(new ArrayList<>(distribution.get(profile)));
        metricsSummaryTable.clear();
    }

    private void showMetrics(JavaClass javaClass) {
        metricsSummaryTable.set(javaClass);
    }

    private void clearPanels() {
//        distribution = null;
        boxChartList = null;
        if (profilesPanel != null) {
            profilesPanel.removeAll();
        }
        if (classesPanel != null) {
            classesPanel.removeAll();
            classesPanel = null;
        }
        if (metricsPanel != null) {
            metricsPanel.removeAll();
        }
        if (mainPanel != null) {
            mainPanel.removeAll();
        }
        if (chartPanel != null) {
            chartPanel.removeAll();
        }
        if (rightPanel != null) {
            rightPanel.removeAll();
        }
        updateUI();
    }

    private void showBoxCharts(@NotNull List<ProfileBoxChartBuilder.BoxChartStructure> boxChartList) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROFILE_BOX_CHART"));

        this.boxChartList = boxChartList;
        List<MetricType> metricTypes = Arrays.stream(MetricType.values())
                .filter(mt -> mt.level() == MetricLevel.CLASS)
                .collect(Collectors.toList());
        MetricTypeTable metricTypeTable = new MetricTypeTable(metricTypes);
        JScrollPane scrollableTablePanel = ScrollPaneFactory.createScrollPane(
                metricTypeTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollableTablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollableTablePanel);
        updateMetricTypeForBoxCharts(metricTypes.get(0));
    }

    private void updateMetricTypeForBoxCharts(@NotNull MetricType metricType) {
        rightPanel.removeAll();
        updateUI();
        chartPanel = new XChartPanel<>(boxChartList.stream()
                .filter(b -> b.getMetricType() == metricType)
                .findFirst()
                .get()
                .getBoxChart());
        JScrollPane scrollablePanel = ScrollPaneFactory.createScrollPane(
                chartPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(scrollablePanel);
    }

    private void showRadarCharts(@NotNull List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts) {
        this.radarCharts = radarCharts;
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        leftPanel = new JBPanel<>(new BorderLayout());

        List<MetricProfile> profiles = radarCharts.stream()
                .map(ProfileRadarChartBuilder.RadarChartStructure::getMetricProfile).collect(Collectors.toList());
        MetricByProfileTable metricByProfileTable = new MetricByProfileTable(profiles);
        JScrollPane scrollablePanel = ScrollPaneFactory.createScrollPane(
                metricByProfileTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        leftPanel.add(scrollablePanel);
        super.setContent(createSplitter(createSplitter(leftPanel, mainPanel, "PROFILE_RADAR_CHART_1"), rightPanel,
                "PROFILE_RADAR_CHART_2"));
        updateMetricProfileForRadarCharts(profiles.get(0));
    }

    private void updateMetricProfileForRadarCharts(@NotNull MetricProfile metricProfile) {
        mainPanel.removeAll();
        rightPanel.removeAll();
        updateUI();

        ProfileRadarChartBuilder.RadarChartStructure chartStructure = radarCharts.stream()
                .filter(b -> b.getMetricProfile() == metricProfile)
                .findFirst()
                .get();
        chartPanel = new XChartPanel<>(chartStructure
                .getRadarChart());
        JScrollPane scrollablePanel = ScrollPaneFactory.createScrollPane(
                chartPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollablePanel);

        ClassesForProfileTable classesForProfileTable = new ClassesForProfileTable(chartStructure.getClasses());
        JScrollPane classesForProfileScrollablePanel = ScrollPaneFactory.createScrollPane(
                classesForProfileTable.getComponent(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.getVerticalScrollBar().setUnitIncrement(10);
        scrollablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        rightPanel.add(classesForProfileScrollablePanel);
    }

    private void showResults(@NotNull CategoryChart categoryChart) {
        mainPanel = new JBPanel<>(new BorderLayout());
        chartPanel = new XChartPanel<>(categoryChart);
        JScrollPane scrollablePanel = ScrollPaneFactory.createScrollPane(
                chartPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollablePanel);
        super.setContent(mainPanel);
    }

    private void showResults(@NotNull HeatMapChart heatMapChart) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROFILE_HEAT_MAP"));

        chartPanel = new XChartPanel<>(heatMapChart);
        JScrollPane scrollablePanel = ScrollPaneFactory.createScrollPane(
                chartPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollablePanel.getHorizontalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollablePanel);
        super.setContent(mainPanel);
    }

    private void showTreeMap() {
        treeMapBottomPanel = new BottomPanel();
        leftPanel = new JBPanel<>(new BorderLayout());
        mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.add(treeMapBottomPanel.getPanel(), BorderLayout.NORTH);
        rightPanel = new JBPanel<>(new BorderLayout());

        super.setContent(createSplitter(createSplitter(leftPanel, mainPanel, "PROFILE_TREE_MAP_1"), rightPanel,
                "PROFILE_TREE_MAP_2"));

        metricProfileList = new MetricProfileList();
        metricProfileList.hideColumn(2);
        metricProfileList.setBorder("Select Profile");
        metricProfileList.setProfiles(new TreeMap<>(distribution));
        leftPanel.add(metricProfileList.getComponent());

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

    private void updateTreeMap(MetricProfile profile) {
        treeMap.setColorProvider(new ProfileColorProvider(distribution.getOrDefault(profile, Set.of())));
        treeMap.updateUI();
        treeMap.refresh();
        treeMapBottomPanel.setData("Metric Profile: " + profile.getName());
    }

    private class MetricsChartEventListener implements MetricsEventListener {

        @Override
        public void metricProfilesIsReady() {
            createProfileUIComponents();
            distribution = MetricTaskCache.instance().getUserData(MetricTaskCache.METRIC_PROFILES);
            if (distribution != null) {
                showProfiles();
            }
        }

        @Override
        public void metricsProfileSelected(MetricProfile profile) {
            if (classesPanel != null) {
                showClasses(profile);
            } else {
                updateTreeMap(profile);
            }
        }

        @Override
        public void javaClassSelected(JavaClass javaClass) {
            showMetrics(javaClass);
        }

        @Override
        public void clearProfilePanel() {
            clearPanels();
        }

        @Override
        public void profilesBoxChartIsReady() {
            List<ProfileBoxChartBuilder.BoxChartStructure> boxChartList = MetricTaskCache.instance()
                    .getUserData(MetricTaskCache.BOX_CHARTS);
            showBoxCharts(Objects.requireNonNull(boxChartList));
        }

        @Override
        public void profilesHeatMapChartIsReady() {
            HeatMapChart heatMapChart = MetricTaskCache.instance().getUserData(MetricTaskCache.HEAT_MAP_CHART);
            showResults(Objects.requireNonNull(heatMapChart));
        }

        @Override
        public void profilesRadarChartIsReady() {
            List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts = MetricTaskCache.instance().getUserData(MetricTaskCache.RADAR_CHART);
            showRadarCharts(Objects.requireNonNull(radarCharts));
        }

        @Override
        public void profilesCategoryChartIsReady() {
            CategoryChart categoryChart = MetricTaskCache.instance().getUserData(MetricTaskCache.PROFILE_CATEGORY_CHART);
            showResults(Objects.requireNonNull(categoryChart));
        }

        @Override
        public void currentMetricType(MetricType metricType) {
            updateMetricTypeForBoxCharts(metricType);
        }

        @Override
        public void currentMetricProfile(MetricProfile metricProfile) {
            updateMetricProfileForRadarCharts(metricProfile);
        }

        @Override
        public void profileTreeMapIsReady() {
            treeMap = MetricTaskCache.instance().getUserData(MetricTaskCache.PROFILE_TREE_MAP);
            distribution = MetricTaskCache.instance().getUserData(MetricTaskCache.METRIC_PROFILES);
            if (treeMap != null && distribution != null) {
                showTreeMap();
            }
        }

        @Override
        public void setProfilePanelBottomText(String text) {
            treeMapBottomPanel.setData(text);
        }

        @Override
        public void profileTreeMapCellClicked(JavaClass javaClass) {
            if (MetricsUtils.isProfileAutoScrollable()) {
                MetricsUtils.openInEditor(javaClass.getPsiClass());
            }
            metricsTrimmedSummaryTable.set(javaClass);
        }
    }
}
