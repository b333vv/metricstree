package org.b333vv.metric.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.awt.GridBagConstraints.*;

public class MetricsValidRangesPanel implements ConfigurationPanel<MetricsValidRangesSettings> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private final Project project;
    private JPanel panel;
    private MetricsValidRangesTable metricsValidRangesTable;
    private JCheckBox controlValidRanges;

    public MetricsValidRangesPanel(Project project, MetricsValidRangesSettings metricsValidRangesSettings) {
        this.project = project;
        createUIComponents(metricsValidRangesSettings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(MetricsValidRangesSettings settings) {
        List<MetricsValidRangeStub> rows = settings.getControlledMetricsList();
        return !rows.equals(metricsValidRangesTable.get())
                || settings.isControlValidRanges() != controlValidRanges.isSelected();
    }

    @Override
    public void save(MetricsValidRangesSettings settings) {
        Map<String, MetricsValidRangeStub> newMetricsMap = metricsValidRangesTable.get()
                .stream()
                .collect(Collectors.toMap(MetricsValidRangeStub::getName, Function.identity()));
        settings.setControlledMetrics(newMetricsMap);
        settings.setControlValidRanges(controlValidRanges.isSelected());
    }

    @Override
    public void load(MetricsValidRangesSettings settings) {
        metricsValidRangesTable.set(settings.getControlledMetricsList());
        controlValidRanges.setSelected(settings.isControlValidRanges());
    }

    private void createUIComponents(MetricsValidRangesSettings metricsValidRangesSettings) {

        controlValidRanges = new JCheckBox("Take Metrics Values Under Control",
                        metricsValidRangesSettings.isControlValidRanges());

        Function<MetricsValidRangeStub, MetricsValidRangeStub> onEdit = value -> {
            EditValidRangeForMetricDialog dialog = new EditValidRangeForMetricDialog(project);
            dialog.setMetricsAllowableValueRangeStub(value);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        Supplier<MetricsValidRangeStub> onAdd = () -> {
            AddValidRangeForMetricDialog dialog = new AddValidRangeForMetricDialog(project);
            if (dialog.showAndGet() && dialog.getMetricsAllowableValueRangeStub() != null) {
                return dialog.getMetricsAllowableValueRangeStub();
            }
            return null;
        };

        metricsValidRangesTable = new MetricsValidRangesTable(EMPTY_LABEL, onEdit, onAdd, project);

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        JPanel tablePanel = metricsValidRangesTable.getComponent();

        panel.add(controlValidRanges, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));

        panel.add(tablePanel, new GridBagConstraints(0, 1, 4, 2, 1.0, 1.0,
                NORTHWEST, BOTH, insets, 40, 40));

        metricsValidRangesTable.enableComponents(tablePanel, controlValidRanges.isSelected());

        controlValidRanges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                metricsValidRangesTable.enableComponents(tablePanel, controlValidRanges.isSelected());
            }
        });
    }
}
