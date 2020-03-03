package org.b333vv.metricsTree.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MetricsTreeSettingsTable {
    private final JBTable table;
    private final JPanel panel;
    private final Model model;
    private Project project;

    public MetricsTreeSettingsTable(String emptyLabel,
                                    Project project,
                                    List<MetricsTreeSettingsStub> rows) {
        this.project = project;
        model = new Model();
        model.setRows(rows);

        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText(emptyLabel);
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);


        table.getTableHeader().setReorderingAllowed(true);

        table.getColumnModel().getColumn(0).setHeaderValue("");
        table.getColumnModel().getColumn(1).setHeaderValue("Metric");
        table.getColumnModel().getColumn(2).setHeaderValue("Metrics Description");
        table.getColumnModel().getColumn(3).setHeaderValue("Metrics Level");
        table.getColumnModel().getColumn(4).setHeaderValue("Metrics Set");

        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(300);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(250);

        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel = new JPanel(new BorderLayout());
        JScrollPane jScrollPane = new JBScrollPane(table);
        Border b = IdeBorderFactory.createTitledBorder("These class level and method level metrics should be calculated");
        jScrollPane.setBorder(b);
        panel.add(jScrollPane);
    }

    public JPanel getComponent() {
        return panel;
    }

    public void set(List<MetricsTreeSettingsStub> data) {
        model.set(data);
    }

    public List<MetricsTreeSettingsStub> get() {
        return new ArrayList<>(model.items());
    }

    private class Model extends AbstractTableModel {
        private final int COLUMN_COUNT = 5;
        private List<MetricsTreeSettingsStub> rows;

        public void setRows(List<MetricsTreeSettingsStub> rows) {
            this.rows = rows;
        }

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
            return column == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            rows.get(rowIndex).setNeedToConsider((Boolean)aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "";
                case 1:
                    return "Metric";
                case 2:
                    return "Metrics Description";
                case 3:
                    return "Metrics Level";
                case 4:
                    return "Metrics Set";
            }
            return "";
        }

        public void set(List<MetricsTreeSettingsStub> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public List<MetricsTreeSettingsStub> items() {
            return rows;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MetricsTreeSettingsStub item = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.isNeedToConsider();
                case 1:
                    return item.getName();
                case 2:
                    return item.getDescription();
                case 3:
                    return item.getLevel();
                case 4:
                    return item.getSet();
            }
            return item;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return Boolean.class;
            }
            return String.class;
        }
    }
}
