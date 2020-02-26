package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;

public class ProjectMetricsTreeSettingsPanel implements ConfigurationPanel<ProjectMetricsTreeSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsTreeSettingsTable metricsTreeSettingsTable;

    public ProjectMetricsTreeSettingsPanel(Project project, ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        this.project = project;
        createUIComponents(projectMetricsTreeSettings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(ProjectMetricsTreeSettings settings) {
        List<MetricsTreeSettingsStub> rows = settings.getMetricsList();
        return !rows.equals(metricsTreeSettingsTable.get());
    }

    @Override
    public void save(ProjectMetricsTreeSettings settings) {
        settings.setProjectTreeMetrics(metricsTreeSettingsTable.get());
    }

    @Override
    public void load(ProjectMetricsTreeSettings settings) {
        metricsTreeSettingsTable.set(settings.getMetricsList());
    }

    private void createUIComponents(ProjectMetricsTreeSettings projectMetricsTreeSettings) {
        metricsTreeSettingsTable = new MetricsTreeSettingsTable(EMPTY_LABEL, project, projectMetricsTreeSettings.getMetricsList());
        panel = metricsTreeSettingsTable.getComponent();
    }
}
