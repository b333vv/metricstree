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
import com.intellij.ui.components.JBPanel;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.ui.info.MetricsSummaryTable;
import org.b333vv.metric.ui.profile.ClassesByProfileTable;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.profile.MetricProfileList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class MetricsProfilePanel extends SimpleToolWindowPanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "SPLIT_PROPORTION";

    private JBPanel<?> profilesPanel;
    private JBPanel<?> classesPanel;
    private JBPanel<?> metricsPanel;

    private MetricProfileList metricProfileList;
    private ClassesByProfileTable classesTable;
    private MetricsSummaryTable metricsSummaryTable;

    private final Project project;
    private Map<MetricProfile, Set<JavaClass>> distribution;

    public MetricsProfilePanel(Project project) {
        super(false, true);
        this.project = project;
        createUIComponents();
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.MetricsProfileToolbar"), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());
        MetricsEventListener metricsEventListener = new MetricsChartEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    private void createUIComponents() {
        profilesPanel = new JBPanel<>(new BorderLayout());
        metricProfileList = new MetricProfileList();
        profilesPanel.add(metricProfileList.getComponent());

        classesPanel = new JBPanel<>(new BorderLayout());
        classesTable = new ClassesByProfileTable();
        classesPanel.add(classesTable.getComponent());

        metricsPanel = new JBPanel<>(new BorderLayout());
        metricsSummaryTable = new MetricsSummaryTable(false);
        metricsPanel.add(metricsSummaryTable.getComponent());

        super.setContent(createSplitter(profilesPanel, createSplitter(classesPanel, metricsPanel)));
    }

    private JComponent createSplitter(JComponent c1, JComponent c2) {
        float savedProportion = PropertiesComponent.getInstance(project)
                .getFloat(MetricsProfilePanel.SPLIT_PROPORTION_PROPERTY, (float) 0.35);

        final JBSplitter splitter = new JBSplitter(false);
        splitter.setFirstComponent(c1);
        splitter.setSecondComponent(c2);
        splitter.setProportion(savedProportion);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.addPropertyChangeListener(Splitter.PROP_PROPORTION,
                evt -> PropertiesComponent.getInstance(project).setValue(MetricsProfilePanel.SPLIT_PROPORTION_PROPERTY,
                        Float.toString(splitter.getProportion())));
        return splitter;
    }

    public void setDistribution(Map<MetricProfile, Set<JavaClass>> distribution) {
        this.distribution = distribution;
    }

    private void showProfiles(Map<MetricProfile, Set<JavaClass>> distribution) {
        metricProfileList.setProfiles(new TreeMap<>(distribution));
    }

    private void showClasses(MetricProfile profile) {
        classesTable.setClasses(new ArrayList<>(distribution.get(profile)));
        metricsSummaryTable.clear();
    }

    private void showMetrics(JavaClass javaClass) {
        metricsSummaryTable.set(javaClass);
    }

    private void clear() {
        distribution = null;
        profilesPanel.removeAll();
        classesPanel.removeAll();
        metricsPanel.removeAll();
        updateUI();
        createUIComponents();
    }

    private class MetricsChartEventListener implements MetricsEventListener {

        @Override
        public void metricProfilesIsReady() {
            Map<MetricProfile, Set<JavaClass>> distribution = MetricTaskCache.instance().getUserData(MetricTaskCache.METRIC_PROFILES);
            setDistribution(distribution);
            showProfiles(distribution);
        }

        @Override
        public void metricsProfileSelected(MetricProfile profile) {
            showClasses(profile);
        }

        @Override
        public void javaClassSelected(JavaClass javaClass) {
            showMetrics(javaClass);
        }

        @Override
        public void clearProfilesPanel() {
            clear();
        }
    }
}
