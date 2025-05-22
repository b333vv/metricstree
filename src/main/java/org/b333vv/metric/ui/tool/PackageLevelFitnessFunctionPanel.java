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
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.ui.fitnessfunction.*;
import org.b333vv.metric.ui.info.*;
import org.b333vv.metric.ui.settings.fitnessfunction.FitnessFunctionItem;
import org.b333vv.metric.ui.settings.fitnessfunction.PackageLevelFitnessFunctions;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class PackageLevelFitnessFunctionPanel extends SimpleToolWindowPanel {
    private JBPanel<?> fitnessFunctionPanel;
    private JBPanel<?> packagesPanel;
    private JBPanel<?> classesPanel;
    private JBPanel<?> metricsPanel;
    private JPanel chartPanel;
    private PackageMetricsTable packageMetricsTable;

    private final BottomPanel bottomPanel = new BottomPanel();

    private JBPanel<?> rightPanel;
    private JBPanel<?> mainPanel;
    private JBPanel<?> leftPanel;

    private PackageLevelFitnessFunctionList packageLevelFitnessFunctionList;
    private PackageLevelFitnessFunctionPackageTable packagesTable;
    private PackageLevelFitnessFunctionClassTable classesTable;
    private MetricsSummaryTable metricsSummaryTable;

    private final Project project;
    private Map<FitnessFunction, Set<JavaPackage>> fitnessFunctionResult;
    private final Map<String, String> fitnessFunctionDescriptionMap;

    private MetricsTrimmedSummaryTable metricsTrimmedSummaryTable;
    private BottomPanel treeMapBottomPanel;
    private MetricTreeMap<JavaCode> treeMap;

    public PackageLevelFitnessFunctionPanel(Project project) {
        super(false, true);
        this.project = project;
        PackageLevelFitnessFunctions packageLevelFitnessFunctions = project.getService(PackageLevelFitnessFunctions.class);
        fitnessFunctionDescriptionMap = packageLevelFitnessFunctions.getProfilesDescription();
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.PackageLevelFitnessFunctionToolbar"), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());
        MetricsEventListener metricsEventListener = new PackageLevelFitnessFunctionEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    private void createProfileUIComponents() {
        fitnessFunctionPanel = new JBPanel<>(new BorderLayout());
        packageLevelFitnessFunctionList = new PackageLevelFitnessFunctionList(this.project);
        fitnessFunctionPanel.add(packageLevelFitnessFunctionList.getComponent());
        fitnessFunctionPanel.add(bottomPanel.getPanel(), BorderLayout.SOUTH);

        packagesPanel = new JBPanel<>(new BorderLayout());
        packagesTable = new PackageLevelFitnessFunctionPackageTable(this.project);
        packagesPanel.add(packagesTable.getComponent());

        classesPanel = new JBPanel<>(new BorderLayout());
        classesTable = new PackageLevelFitnessFunctionClassTable(this.project);
        classesPanel.add(classesTable.getComponent());

        metricsPanel = new JBPanel<>(new BorderLayout());
        metricsSummaryTable = new MetricsSummaryTable(false);
        metricsPanel.add(metricsSummaryTable.getComponent());

        super.setContent(createSplitter(fitnessFunctionPanel,
                createSplitter(packagesPanel,
                        createSplitter(classesPanel, metricsPanel, "PROFILE_2"),
                        "PROFILE_3"),
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

    public void setDistribution(Map<FitnessFunction, Set<JavaPackage>> fitnessFunctionResult) {
        this.fitnessFunctionResult = fitnessFunctionResult;
    }

    private void showResult() {
        packageLevelFitnessFunctionList.setProfiles(new TreeMap<>(fitnessFunctionResult));
    }

    private void showResults(XYChart xyChart, Map<String, Double> instability, Map<String, Double> abstractness) {
        mainPanel = new JBPanel<>(new BorderLayout());
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel, "PROJECT_XY_CHART"));

        chartPanel = new XChartPanel<>(xyChart);
        PackageLevelFitnessFunctionPanel.CoordinateListener mouseListener = new PackageLevelFitnessFunctionPanel.CoordinateListener(xyChart);
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

    private void showPackages(FitnessFunction profile) {
        packagesTable.setPackages(new ArrayList<>(fitnessFunctionResult.get(profile)));
        bottomPanel.setData(fitnessFunctionDescriptionMap.get(profile.name()));
        metricsSummaryTable.clear();
        classesTable.clear();
    }

    private void showMetrics(JavaClass javaClass) {
        metricsSummaryTable.set(javaClass);
    }

    private void showClasses(JavaPackage javaPackage) {
        classesTable.set(javaPackage);
    }

    private void clearPanels() {
        if (fitnessFunctionPanel != null) {
            fitnessFunctionPanel.removeAll();
        }
        if (packagesPanel != null) {
            packagesPanel.removeAll();
            packagesPanel = null;
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
        if (rightPanel != null) {
            rightPanel.removeAll();
        }
        updateUI();
    }

    private void showTreeMap() {
        treeMapBottomPanel = new BottomPanel();
        leftPanel = new JBPanel<>(new BorderLayout());
        mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.add(treeMapBottomPanel.getPanel(), BorderLayout.NORTH);
        rightPanel = new JBPanel<>(new BorderLayout());

        super.setContent(createSplitter(createSplitter(leftPanel, mainPanel, "PROFILE_TREE_MAP_1"), rightPanel,
                "PROFILE_TREE_MAP_2"));

        packageLevelFitnessFunctionList = new PackageLevelFitnessFunctionList(this.project);
        packageLevelFitnessFunctionList.hideColumn(2);
        packageLevelFitnessFunctionList.setBorder("Select Profile");
        packageLevelFitnessFunctionList.setProfiles(new TreeMap<>(fitnessFunctionResult));
        leftPanel.add(packageLevelFitnessFunctionList.getComponent());

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

    private class PackageLevelFitnessFunctionEventListener implements MetricsEventListener {

        @Override
        public void packageLevelFitnessFunctionIsReady() {
            createProfileUIComponents();
            fitnessFunctionResult = project.getService(MetricTaskCache.class).getUserData(MetricTaskCache.PACKAGE_LEVEL_FITNESS_FUNCTION);
            if (fitnessFunctionResult != null) {
                showResult();
            }
        }

        @Override
        public void xyChartIsReady() {
            Map<String, Double> instability = project.getService(MetricTaskCache.class).getUserData(MetricTaskCache.INSTABILITY);
            Map<String, Double> abstractness = project.getService(MetricTaskCache.class).getUserData(MetricTaskCache.ABSTRACTNESS);
            XYChart xyChart = project.getService(MetricTaskCache.class).getUserData(MetricTaskCache.XY_CHART);
            showResults(xyChart, instability, abstractness);
        }

        @Override
        public void packageLevelFitnessFunctionSelected(FitnessFunction fitnessFunction) {
            if (packagesPanel != null) {
                showPackages(fitnessFunction);
            } else {
//                updateTreeMap(fitnessFunction);
            }
        }

        @Override
        public void packageLevelJavaClassSelected(JavaClass javaClass) {
            showMetrics(javaClass);
        }

        @Override
        public void javaPackageSelected(JavaPackage javaPackage) {
            showClasses(javaPackage);
        }

        @Override
        public void clearPackageFitnessFunctionPanel() {
            clearPanels();
        }

    }
    public class CoordinateListener extends MouseAdapter {
        private final XYChart chart;
        public CoordinateListener(XYChart chart) {
            this.chart = chart;
        }
        @Override
        public void mousePressed(MouseEvent e) {
            double chartX = chart.getChartXFromCoordinate(e.getX());
            double chartY = chart.getChartYFromCoordinate(e.getY());
            packageMetricsTable.updateSelection(chartX, chartY);
        }
    }
}
