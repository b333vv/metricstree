package org.jacoquev.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
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

public class MetricsAllowableValuesRangesTable {
    private final Function<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub, MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> onEdit;
    private final JBTable table;
    private final JPanel panel;
    private final Model model;
    private Project project;

    public MetricsAllowableValuesRangesTable(String emptyLabel,
                                             Function<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub, MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> onEdit,
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

    public void set(List<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> data) {
        model.set(data);
    }

    public List<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> get() {
        return new ArrayList<>(model.items());
    }

    private void editEntry() {
        int selectedIndex = table.getSelectedRow();

        if (selectedIndex >= 0) {
            MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub value = model.items().get(selectedIndex);
            MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub newValue = onEdit.apply(value);
            if (newValue != null) {
                model.items().set(selectedIndex, newValue);
            }
        }
    }

    private class Model extends AbstractTableModel {
        private final int COLUMN_COUNT = 3;
        MetricsAllowableValuesRanges metricsAllowableValuesRanges = MetricsUtils.get(MetricsAllowableValuesRangesTable.this.project, MetricsAllowableValuesRanges.class);
        private List<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> rows = metricsAllowableValuesRanges.getMetricsList();

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

        public void set(List<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public List<MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub> items() {
            return rows;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MetricsAllowableValuesRanges.MetricsAllowableValueRangeStub item = rows.get(rowIndex);
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
