package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;

public class ClassMetricsTreeSettingsPanel implements ConfigurationPanel<ClassMetricsTreeSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsTreeSettingsTable metricsTreeSettingsTable;

    public ClassMetricsTreeSettingsPanel(Project project, ClassMetricsTreeSettings classMetricsTreeSettings) {
        this.project = project;
        createUIComponents(classMetricsTreeSettings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(ClassMetricsTreeSettings settings) {
        List<MetricsTreeSettingsStub> rows = settings.getMetricsList();
        return !rows.equals(metricsTreeSettingsTable.get());
    }

    @Override
    public void save(ClassMetricsTreeSettings settings) {
        settings.setClassTreeMetrics(metricsTreeSettingsTable.get());
    }

    @Override
    public void load(ClassMetricsTreeSettings settings) {
        metricsTreeSettingsTable.set(settings.getMetricsList());
    }

    private void createUIComponents(ClassMetricsTreeSettings classMetricsTreeSettings) {
        metricsTreeSettingsTable = new MetricsTreeSettingsTable(EMPTY_LABEL, project, classMetricsTreeSettings.getMetricsList());
        panel = metricsTreeSettingsTable.getComponent();
    }
}
