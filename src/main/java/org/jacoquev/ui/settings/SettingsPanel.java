package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import org.jacoquev.util.MetricsSettings;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel {
    private final JPanel root;
    private final MetricsSettingsPanel metricsSettingsPanel;
    private final JPanel rootPropertiesPane;
    private final Project project;

    public SettingsPanel(Project project) {
        this.project = project;
        root = new JPanel(new BorderLayout());
        JBTabbedPane tabs = new JBTabbedPane();

        metricsSettingsPanel = new MetricsSettingsPanel(project);
        rootPropertiesPane = new JPanel(new BorderLayout());

        tabs.insertTab("Metrics settings", null, metricsSettingsPanel.getComponent(), "Configure metrics", 0);
        tabs.insertTab("Quality model settings", null, rootPropertiesPane, "Configure quality model settings", 1);

        root.add(tabs, BorderLayout.CENTER);
    }

    public Project getProject() {
        return project;
    }

    public JComponent getRootPane() {
        return root;
    }

    public boolean isModified(MetricsSettings metricsSettings) {
        return metricsSettingsPanel.isModified(metricsSettings);
    }

    public void save(MetricsSettings metricsSettings) {
        metricsSettingsPanel.save(metricsSettings);
    }
}
