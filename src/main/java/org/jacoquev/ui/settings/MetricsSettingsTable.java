package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jacoquev.util.MetricsSettings;
import org.jacoquev.util.MetricsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class MetricsSettingsTable {
    private final Function<MetricsSettings.MetricStub, MetricsSettings.MetricStub> onEdit;
    private final JBTable table;
    private final JPanel panel;
    private final Model model;
    private HashMap<String, MetricsSettings.MetricStub> updatedMetrics = new HashMap<>();
    private Project project;

    public MetricsSettingsTable(String emptyLabel,
                                Function<MetricsSettings.MetricStub, MetricsSettings.MetricStub> onEdit,
                                Project project) {
        this.project = project;

        this.onEdit = onEdit;

        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText(emptyLabel);
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);

        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editEntry();
                }
            }
        });

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table)
                .setEditActionName("Edit")
                .setEditAction(e -> editEntry())
                .disableUpDownActions();

        panel = new JPanel(new BorderLayout());
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return panel;
    }

    public void set(List<MetricsSettings.MetricStub> data) {
        model.set(data);
    }

    public List<MetricsSettings.MetricStub> get() {
        return new ArrayList<>(model.items());
    }

    public HashMap<String, MetricsSettings.MetricStub> getUpdatedMetrics() {
        return updatedMetrics;
    }

    private void editEntry() {
        int selectedIndex = table.getSelectedRow();

        if (selectedIndex >= 0) {
            MetricsSettings.MetricStub value = model.items().get(selectedIndex);
            MetricsSettings.MetricStub newValue = onEdit.apply(value);
            if (newValue != null) {
                model.items().set(selectedIndex, newValue);
                updatedMetrics.put(newValue.getName(), newValue);
            }
        }
    }

    private class Model extends AbstractTableModel {
        private final int COLUMN_COUNT = 3;
        MetricsSettings metricsSettings = MetricsUtils.get(MetricsSettingsTable.this.project, MetricsSettings.class);
        private List<MetricsSettings.MetricStub> rows = metricsSettings.getMetricsList();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Min value";
                case 2:
                    return "Max value";
            }
            return "";
        }

        public void set(List<MetricsSettings.MetricStub> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public List<MetricsSettings.MetricStub> items() {
            return rows;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MetricsSettings.MetricStub item = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getDescription();
                case 1:
                    return item.isDoubleValue() ? item.getMinDoubleValue() : item.getMinLongValue();
                case 2:
                    return item.isDoubleValue() ? item.getMaxDoubleValue() : item.getMaxLongValue();
            }
            return item;
        }
    }
}
