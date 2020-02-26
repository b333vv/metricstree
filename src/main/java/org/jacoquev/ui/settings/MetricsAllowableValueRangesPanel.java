package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetricsAllowableValueRangesPanel implements ConfigurationPanel<MetricsAllowableValueRanges> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsAllowableValueRangesTable metricsAllowableValueRangesTable;

    public MetricsAllowableValueRangesPanel(Project project) {
        this.project = project;
        createUIComponents();
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(MetricsAllowableValueRanges settings) {
        List<MetricsAllowableValueRanges.MetricsAllowableValueRangeStub> rows = settings.getMetricsList();
        return !rows.equals(metricsAllowableValueRangesTable.get());
    }

    @Override
    public void save(MetricsAllowableValueRanges settings) {
        Map<String, MetricsAllowableValueRanges.MetricsAllowableValueRangeStub> newMetricsMap = metricsAllowableValueRangesTable.get()
                .stream()
                .collect(Collectors.toMap(MetricsAllowableValueRanges.MetricsAllowableValueRangeStub::getName, Function.identity()));
        settings.setMetrics(newMetricsMap);
    }

    @Override
    public void load(MetricsAllowableValueRanges settings) {
        metricsAllowableValueRangesTable.set(settings.getMetricsList());
    }

    private void createUIComponents() {
        Function<MetricsAllowableValueRanges.MetricsAllowableValueRangeStub, MetricsAllowableValueRanges.MetricsAllowableValueRangeStub> onEdit = value -> {
            EditMetricsDialog dialog = new EditMetricsDialog(project);
            dialog.setMetricsAllowableValueRangeStub(value);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        metricsAllowableValueRangesTable = new MetricsAllowableValueRangesTable(EMPTY_LABEL, onEdit, project);
        panel = metricsAllowableValueRangesTable.getComponent();
    }
}
