package org.b333vv.metric.ui.info;

import com.intellij.icons.AllIcons;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.Sets;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClassOrMethodMetricsTable {
    private final Model model;
    private final JBScrollPane panel;
    private final JBTable table;

    public ClassOrMethodMetricsTable() {
        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);

        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(130);
        table.getColumnModel().getColumn(4).setMaxWidth(200);

        panel = new JBScrollPane(table);
    }

    private void hideOrShowValidValuesColumn(boolean controlValidRanges) {
        if (controlValidRanges) {
            table.getColumnModel().getColumn(0).setWidth(30);
            table.getColumnModel().getColumn(0).setMinWidth(30);
            table.getColumnModel().getColumn(0).setMaxWidth(30);

            table.getColumnModel().getColumn(4).setWidth(200);
            table.getColumnModel().getColumn(4).setMinWidth(200);
            table.getColumnModel().getColumn(4).setMaxWidth(200);
        } else {
            table.getColumnModel().getColumn(0).setWidth(0);
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);

            table.getColumnModel().getColumn(4).setWidth(0);
            table.getColumnModel().getColumn(4).setMinWidth(0);
            table.getColumnModel().getColumn(4).setMaxWidth(0);
        }
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    public void set(JavaCode javaCode) {
        String prefix = "";
        if (javaCode instanceof JavaProject) {
            prefix = "Project: ";
        } else if (javaCode instanceof JavaPackage) {
            prefix = "Package: ";
        } else if (javaCode instanceof JavaClass) {
            prefix = "Class: ";
        } else if (javaCode instanceof JavaMethod) {
            prefix = "Method: ";
        }
        Border b = IdeBorderFactory.createTitledBorder(prefix + javaCode.getName());
        panel.setBorder(b);
        List<Metric> sortedMetrics = javaCode.getMetrics()
                .sorted(Comparator.comparing(Metric::getName))
                .collect(Collectors.toList());
        model.set(sortedMetrics);
        hideOrShowValidValuesColumn(MetricsService.isControlValidRanges());
    }

    private static class Model extends AbstractTableModel {

        private transient List<Metric> rows = List.of();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 1:
                    return "Metric";
                case 2:
                    return "Description";
                case 3:
                    return "Value";
                case 4:
                    return "Valid";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
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
                    return metric.getName();
                case 2:
                    return metric.getDescription();
                case 3:
                    if (Sets.inMoodMetricsSet(metric.getName())) {
                        return metric.getValue().percentageFormat();
                    } else {
                        return metric.getFormattedValue();
                    }
                case 4:
                    if (Sets.inMoodMetricsSet(metric.getName())) {
                        return metric.getRange().percentageFormat();
                    } else {
                        return metric.getRange();
                    }
                default:
                    return metric;
            }
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
