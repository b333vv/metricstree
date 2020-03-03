package org.b333vv.metricsTree.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metricsTree.util.MetricsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetricsAllowableValuesRangesTable {
    private final Function<MetricsAllowableValuesRangeStub,
            MetricsAllowableValuesRangeStub> onEdit;
    private final Supplier<MetricsAllowableValuesRangeStub> onAdd;
    private final JBTable table;
    private final JPanel panel;
    private final Model model;
    private Project project;

    public MetricsAllowableValuesRangesTable(String emptyLabel,
                                             Function<MetricsAllowableValuesRangeStub,
                                                     MetricsAllowableValuesRangeStub> onEdit,
                                             Supplier<MetricsAllowableValuesRangeStub> onAdd,
                                             Project project) {
        this.project = project;
        this.onEdit = onEdit;
        this.onAdd = onAdd;

        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText(emptyLabel);
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);

        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(300);
        table.getColumnModel().getColumn(2).setMaxWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editEntry();
                }
            }
        });

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table)
                .setAddActionName("Add")
                .setAddAction(e -> addEntry())
                .setEditActionName("Edit")
                .setEditAction(e -> editEntry())
                .setRemoveActionName("Remove")
                .setRemoveAction(e -> removeEntry())
                .disableUpDownActions();

        panel = new JPanel(new BorderLayout());
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return panel;
    }

    public void set(List<MetricsAllowableValuesRangeStub> data) {
        model.set(data);
    }

    public List<MetricsAllowableValuesRangeStub> get() {
        return new ArrayList<>(model.items());
    }

    private void editEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            MetricsAllowableValuesRangeStub value = model.items().get(selectedIndex);
            MetricsAllowableValuesRangeStub newValue = onEdit.apply(value);
            if (newValue != null) {
                model.items().set(selectedIndex, newValue);
            }
        }
    }

    private void addEntry() {
        MetricsAllowableValuesRangeStub newValue = onAdd.get();
        if (newValue != null) {
            model.items().add(newValue);
        }
    }

    private void removeEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            MetricsAllowableValuesRangeStub value = model.items().get(selectedIndex);
            MetricsAllowableValuesRangesSettings metricsAllowableValuesRangesSettings =
                    MetricsUtils.get(MetricsAllowableValuesRangesTable.this.project, MetricsAllowableValuesRangesSettings.class);
            metricsAllowableValuesRangesSettings.addToUnControlledMetrics(value);
            model.items().remove(value);
        }
    }

    private class Model extends AbstractTableModel {
        private final int COLUMN_COUNT = 5;
        MetricsAllowableValuesRangesSettings metricsAllowableValuesRangesSettings =
                MetricsUtils.get(MetricsAllowableValuesRangesTable.this.project, MetricsAllowableValuesRangesSettings.class);
        private List<MetricsAllowableValuesRangeStub> rows = metricsAllowableValuesRangesSettings.getControlledMetricsList();

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
                    return "Metric";
                case 1:
                    return "Description";
                case 2:
                    return "Level";
                case 3:
                    return "Min value";
                case 4:
                    return "Max value";
            }
            return "";
        }

        public void set(List<MetricsAllowableValuesRangeStub> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public List<MetricsAllowableValuesRangeStub> items() {
            return rows;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MetricsAllowableValuesRangeStub item = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getName();
                case 1:
                    return item.getDescription();
                case 2:
                    return item.getLevel();
                case 3:
                    return item.isDoubleValue() ? item.getMinDoubleValue() : item.getMinLongValue();
                case 4:
                    return item.isDoubleValue() ? item.getMaxDoubleValue() : item.getMaxLongValue();
            }
            return item;
        }
    }
}
