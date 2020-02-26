package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import org.jacoquev.util.ClassMetricsTreeSettings;
import org.jacoquev.util.MetricsAllowableValueRanges;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final MetricsAllowableValueRangesPanel metricsAllowableValueRangesPanel;
    private final ClassMetricsTreePanel classMetricsTreePanel;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        metricsAllowableValueRangesPanel = new MetricsAllowableValueRangesPanel(project);
        classMetricsTreePanel = new ClassMetricsTreePanel(project);

        tabs.insertTab("Metrics Allowed Values", null, metricsAllowableValueRangesPanel.getComponent(), "Configure allowed values ", 0);
        tabs.insertTab("Class Metrics Tree", null, classMetricsTreePanel.getComponent(), "Configure class metrics tree", 1);
//        tabs.insertTab("Project Metrics Tree", null, rootPropertiesPane, "Configure project metrics tree", 2);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(MetricsAllowableValueRanges metricsAllowableValueRanges) {
        return metricsAllowableValueRangesPanel.isModified(metricsAllowableValueRanges);
    }

    public boolean isModified(ClassMetricsTreeSettings classMetricsTreeSettings) {
        return classMetricsTreePanel.isModified(classMetricsTreeSettings);
    }

    public void save(MetricsAllowableValueRanges metricsAllowableValueRanges) {
        metricsAllowableValueRangesPanel.save(metricsAllowableValueRanges);
    }

    public void save(ClassMetricsTreeSettings classMetricsTreeSettings) {
        classMetricsTreePanel.save(classMetricsTreeSettings);
    }
}
