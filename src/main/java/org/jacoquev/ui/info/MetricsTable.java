package org.jacoquev.ui.info;

import com.intellij.icons.AllIcons;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jacoquev.model.code.JavaCode;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.Sets;
import org.jacoquev.model.metric.value.Range;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetricsTable {
    private final JBTable table;
    private final Model model;
    private final JBScrollPane panel;

    public MetricsTable() {
        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);

        table.getColumnModel().getColumn(0).setHeaderValue("");
        table.getColumnModel().getColumn(1).setHeaderValue("Metric name");
        table.getColumnModel().getColumn(2).setHeaderValue("Value");
        table.getColumnModel().getColumn(3).setHeaderValue("Allowable value range");

        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(200);

        panel = new JBScrollPane(table);
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void init(JavaProject javaProject) {
        set(javaProject);
    }

    public void clear() {
        model.set(Collections.EMPTY_LIST);
    }

    public void set(JavaCode javaCode) {
        Border b = IdeBorderFactory.createTitledBorder(javaCode.getName());
        panel.setBorder(b);
        List<Metric> sortedMetrics = javaCode.getMetrics()
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                .collect(Collectors.toList());
        model.set(sortedMetrics);
    }

    private class Model extends AbstractTableModel {

        private final int COLUMN_COUNT = 4;
        private List<Metric> rows = Collections.EMPTY_LIST;

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
                    return "";
                case 1:
                    return "Name";
                case 2:
                    return "Value";
                case 3:
                    return "Allowed value range";
            }
            return "";
        }

        @Override
        public Class getColumnClass(int column) {
            if (column == 0) {
                return Icon.class;
            }
            return String.class;
        }

        public void set(List<Metric> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            Metric metric = rows.get(row);
            switch (column) {
                case 0:
                    return getRowIcon(metric);
                case 1:
                    return metric.getDescription();
                case 2:
                    if (Sets.inMoodMetricsSet(metric.getName())) {
                        return metric.getValue().percentageFormat();
                    } else {
                        return metric.getFormattedValue();
                    }
                case 3:
                    if (Sets.inMoodMetricsSet(metric.getName())) {
                        return metric.getRange().percentageFormat();
                    } else {
                        return metric.getRange();
                    }
            }
            return metric;
        }

        private Icon getRowIcon(Metric metric) {
            if (!metric.hasAllowableValue()) {
                return AllIcons.General.BalloonError;
            } else if (metric.getRange() == Range.UNDEFINED) {
                return AllIcons.General.BalloonWarning;
            }
            return AllIcons.Actions.Commit;
        }
    }

}
