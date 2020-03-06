package org.b333vv.metric.ui.settings;

import javax.swing.*;
import java.util.List;

public class ClassMetricsTreeSettingsPanel implements ConfigurationPanel<ClassMetricsTreeSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private JPanel panel;
    private MetricsTreeSettingsTable metricsTreeSettingsTable;

    public ClassMetricsTreeSettingsPanel(ClassMetricsTreeSettings classMetricsTreeSettings) {
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
        metricsTreeSettingsTable = new MetricsTreeSettingsTable(EMPTY_LABEL, classMetricsTreeSettings.getMetricsList());
        panel = metricsTreeSettingsTable.getComponent();
    }
}
