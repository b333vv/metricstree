/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.ui.info;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.MetricsIcons;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.value.BasicMetricsRange;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MetricsTrimmedSummaryTable {
    private final Model model;
    private final JBScrollPane panel;
    private final JBTable table;

    public MetricsTrimmedSummaryTable() {
        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setMaxWidth(15);
        table.getColumnModel().getColumn(1).setMaxWidth(250);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(50);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        panel = new JBScrollPane(table);
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        panel.setBorder(IdeBorderFactory.createTitledBorder(""));
        model.set(List.of());
    }

    public void set(JavaClass javaClass) {
        Border b = IdeBorderFactory.createTitledBorder("Class: " + javaClass.getName());
        panel.setBorder(b);
        List<Metric> sortedMetrics = javaClass.metrics()
                .sorted(Comparator.comparing(m -> m.getType().description()))
                .collect(Collectors.toList());
        model.set(sortedMetrics);
        model.fireTableDataChanged();
    }

    private static class Model extends AbstractTableModel {
        private List<Metric> rows = List.of();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
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
                    return "Value";
                case 3:
                    return "Excess";
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
                    return metric.getType().description();
                case 2:
                    return metric.getFormattedValue();
                case 3:
                    return getExcess(metric);
                default:
                    return metric;
            }
        }

        private String getExcess(Metric metric) {
            if (metric.getValue() == Value.UNDEFINED) {
                return "-";
            }
            Value to = MetricsService.getRangeForMetric(metric.getType()).getRegularTo();
            Value from = MetricsService.getRangeForMetric(metric.getType()).getRegularFrom();
            if (to == Value.UNDEFINED || from == Value.UNDEFINED) {
                return "-";
            }
            if (metric.getValue().isLessThan(from)) {
                return "-" + from.minus(metric.getValue());
            }
            if (MetricsService.getRangeForMetric(metric.getType()) instanceof BasicMetricsRange) {
                if (metric.getValue().isEqualsOrGreaterThan(to)) {
                    return "+" + metric.getValue().minus(to).plus(Value.ONE);
                }
            } else {
                if (metric.getValue().isGreaterThan(to)) {
                    return "+" + metric.getValue().minus(to);
                }
            }
            return "";
        }

        private Icon getRowIcon(Metric metric) {
            if (metric.getValue() == Value.UNDEFINED) {
                return MetricsIcons.NA;
            }
            if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.REGULAR) {
                return MetricsIcons.REGULAR_COLOR;
            }
            if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.HIGH) {
                return MetricsIcons.HIGH_COLOR;
            }
            if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.VERY_HIGH) {
                return MetricsIcons.VERY_HIGH_COLOR;
            }
            if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.EXTREME) {
                return MetricsIcons.EXTREME_COLOR;
            }
            return MetricsIcons.NOT_TRACKED;
        }
    }
}
