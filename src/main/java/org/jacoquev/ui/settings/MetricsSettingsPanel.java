package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import org.jacoquev.util.MetricsSettings;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetricsSettingsPanel implements ConfigurationPanel<MetricsSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsSettingsTable metricsSettingsTable;

    public MetricsSettingsPanel(Project project) {
        this.project = project;
        createUIComponents();
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(MetricsSettings settings) {
        List<MetricsSettings.MetricStub> rows = settings.getMetricsList();
        return !rows.equals(metricsSettingsTable.get());
    }

    @Override
    public void save(MetricsSettings settings) {
        Map<String, MetricsSettings.MetricStub> newMetricsMap = metricsSettingsTable.get()
                .stream()
                .collect(Collectors.toMap(MetricsSettings.MetricStub::getName, Function.identity()));
        settings.setMetrics(newMetricsMap);
    }

    @Override
    public void load(MetricsSettings settings) {
        metricsSettingsTable.set(settings.getMetricsList());
    }

    private void createUIComponents() {
        Function<MetricsSettings.MetricStub, MetricsSettings.MetricStub> onEdit = value -> {
            EditMetricsDialog dialog = new EditMetricsDialog(project);
            dialog.setMetricStub(value);
            if (dialog.showAndGet() && dialog.getMetricStub() != null) {
                return dialog.getMetricStub();
            }
            return null;
        };

        metricsSettingsTable = new MetricsSettingsTable(EMPTY_LABEL, onEdit, project);
        panel = metricsSettingsTable.getComponent();
    }
}
