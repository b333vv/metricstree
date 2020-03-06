package org.b333vv.metric.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final MetricsValidRangesPanel metricsValidRangesPanel;
    private final ClassMetricsTreeSettingsPanel classMetricsTreeSettingsPanel;
    private final ProjectMetricsTreeSettingsPanel projectMetricsTreeSettingsPanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        MetricsValidRangesSettings metricsValidRangesSettings =
                MetricsUtils.get(project, MetricsValidRangesSettings.class);
        ClassMetricsTreeSettings classMetricsTreeSettings =
                MetricsUtils.get(project, ClassMetricsTreeSettings.class);
        ProjectMetricsTreeSettings projectMetricsTreeSettings =
                MetricsUtils.get(project, ProjectMetricsTreeSettings.class);

        metricsValidRangesPanel = new MetricsValidRangesPanel(project, metricsValidRangesSettings);
        classMetricsTreeSettingsPanel = new ClassMetricsTreeSettingsPanel(classMetricsTreeSettings);
        projectMetricsTreeSettingsPanel = new ProjectMetricsTreeSettingsPanel(projectMetricsTreeSettings);

        tabs.insertTab("Metrics Valid Values", null, metricsValidRangesPanel.getComponent(),
                "Configure valid values ", 0);
        tabs.insertTab("Class Metrics Tree Composition", null, classMetricsTreeSettingsPanel.getComponent(),
                "Configure class metrics tree composition", 1);
        tabs.insertTab("Project Metrics Tree Composition", null, projectMetricsTreeSettingsPanel.getComponent(),
                "Configure project metrics tree composition", 2);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(MetricsValidRangesSettings metricsValidRangesSettings) {
        return metricsValidRangesPanel.isModified(metricsValidRangesSettings);
    }

    public boolean isModified(ClassMetricsTreeSettings classMetricsTreeSettings) {
        return classMetricsTreeSettingsPanel.isModified(classMetricsTreeSettings);
    }

    public boolean isModified(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        return projectMetricsTreeSettingsPanel.isModified(projectMetricsTreeSettings);
    }

    public void save(MetricsValidRangesSettings metricsValidRangesSettings) {
        metricsValidRangesPanel.save(metricsValidRangesSettings);
    }

    public void save(ClassMetricsTreeSettings classMetricsTreeSettings) {
        classMetricsTreeSettingsPanel.save(classMetricsTreeSettings);
    }

    public void save(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        projectMetricsTreeSettingsPanel.save(projectMetricsTreeSettings);
    }
}
