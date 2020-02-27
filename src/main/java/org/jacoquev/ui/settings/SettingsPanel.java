package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import org.jacoquev.util.MetricsUtils;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final MetricsAllowableValuesRangesPanel metricsAllowableValuesRangesPanel;
    private final ClassMetricsTreeSettingsPanel classMetricsTreeSettingsPanel;
    private final ProjectMetricsTreeSettingsPanel projectMetricsTreeSettingsPanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        ClassMetricsTreeSettings classMetricsTreeSettings =
                MetricsUtils.get(project, ClassMetricsTreeSettings.class);
        ProjectMetricsTreeSettings projectMetricsTreeSettings =
                MetricsUtils.get(project, ProjectMetricsTreeSettings.class);

        metricsAllowableValuesRangesPanel = new MetricsAllowableValuesRangesPanel(project);
        classMetricsTreeSettingsPanel = new ClassMetricsTreeSettingsPanel(project, classMetricsTreeSettings);
        projectMetricsTreeSettingsPanel = new ProjectMetricsTreeSettingsPanel(project, projectMetricsTreeSettings);

        tabs.insertTab("Metrics Allowed Values", null, metricsAllowableValuesRangesPanel.getComponent(), "Configure allowed values ", 0);
        tabs.insertTab("Class Metrics Tree", null, classMetricsTreeSettingsPanel.getComponent(), "Configure class metrics tree", 1);
        tabs.insertTab("Project Metrics Tree", null, projectMetricsTreeSettingsPanel.getComponent(), "Configure project metrics tree", 2);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(MetricsAllowableValuesRanges metricsAllowableValuesRanges) {
        return metricsAllowableValuesRangesPanel.isModified(metricsAllowableValuesRanges);
    }

    public boolean isModified(ClassMetricsTreeSettings classMetricsTreeSettings) {
        return classMetricsTreeSettingsPanel.isModified(classMetricsTreeSettings);
    }

    public boolean isModified(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        return projectMetricsTreeSettingsPanel.isModified(projectMetricsTreeSettings);
    }

    public void save(MetricsAllowableValuesRanges metricsAllowableValuesRanges) {
        metricsAllowableValuesRangesPanel.save(metricsAllowableValuesRanges);
    }

    public void save(ClassMetricsTreeSettings classMetricsTreeSettings) {
        classMetricsTreeSettingsPanel.save(classMetricsTreeSettings);
    }

    public void save(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        projectMetricsTreeSettingsPanel.save(projectMetricsTreeSettings);
    }
}
