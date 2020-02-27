package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetricsAllowableValuesRangesPanel implements ConfigurationPanel<MetricsAllowableValuesRanges> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsAllowableValuesRangesTable metricsAllowableValuesRangesTable;

    public MetricsAllowableValuesRangesPanel(Project project) {
        this.project = project;
        createUIComponents();
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(MetricsAllowableValuesRanges settings) {
        List<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> rows = settings.getMetricsList();
        return !rows.equals(metricsAllowableValuesRangesTable.get());
    }

    @Override
    public void save(MetricsAllowableValuesRanges settings) {
        Map<String, MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> newMetricsMap = metricsAllowableValuesRangesTable.get()
                .stream()
                .collect(Collectors.toMap(MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub::getName, Function.identity()));
        settings.setMetrics(newMetricsMap);
    }

    @Override
    public void load(MetricsAllowableValuesRanges settings) {
        metricsAllowableValuesRangesTable.set(settings.getMetricsList());
    }

    private void createUIComponents() {
        Function<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub, MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> onEdit = value -> {
            EditAllowableValuesRangeForMetricDialog dialog = new EditAllowableValuesRangeForMetricDialog(project);
            dialog.setMetricsAllowableValueRangeStub(value);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        metricsAllowableValuesRangesTable = new MetricsAllowableValuesRangesTable(EMPTY_LABEL, onEdit, project);
        panel = metricsAllowableValuesRangesTable.getComponent();
    }
}
