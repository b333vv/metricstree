package org.b333vv.metric.ui.settings;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MetricsAllowableValuesRangesPanel implements ConfigurationPanel<MetricsAllowableValuesRangesSettings> {
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
    public boolean isModified(MetricsAllowableValuesRangesSettings settings) {
        List<MetricsAllowableValuesRangeStub> rows = settings.getControlledMetricsList();
        return !rows.equals(metricsAllowableValuesRangesTable.get());
    }

    @Override
    public void save(MetricsAllowableValuesRangesSettings settings) {
        Map<String, MetricsAllowableValuesRangeStub> newMetricsMap = metricsAllowableValuesRangesTable.get()
                .stream()
                .collect(Collectors.toMap(MetricsAllowableValuesRangeStub::getName, Function.identity()));
        settings.setControlledMetrics(newMetricsMap);
    }

    @Override
    public void load(MetricsAllowableValuesRangesSettings settings) {
        metricsAllowableValuesRangesTable.set(settings.getControlledMetricsList());
    }

    private void createUIComponents() {

        Function<MetricsAllowableValuesRangeStub, MetricsAllowableValuesRangeStub> onEdit = value -> {
            EditAllowableValuesRangeForMetricDialog dialog = new EditAllowableValuesRangeForMetricDialog(project);
            dialog.setMetricsAllowableValueRangeStub(value);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        Supplier<MetricsAllowableValuesRangeStub> onAdd = () -> {
            AddAllowableValuesRangeForMetricDialog dialog = new AddAllowableValuesRangeForMetricDialog(project);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {

                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        metricsAllowableValuesRangesTable = new MetricsAllowableValuesRangesTable(EMPTY_LABEL, onEdit, onAdd, project);
        panel = metricsAllowableValuesRangesTable.getComponent();
    }
}
