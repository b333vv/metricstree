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
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import icons.MetricsIcons;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MetricsRangesTable {
    private final Model model;
    private final JBScrollPane panel;
    private final Set<MetricType> metricTypes;
    private final JBTable table;

    public MetricsRangesTable(Set<MetricType> metricTypes) {
        this.metricTypes = metricTypes;

        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(300);
        table.getColumnModel().getColumn(2).setMaxWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(100);

        Border headerBorder = UIManager.getBorder("TableHeader.cellBorder");
        JLabel regularLabel = new JLabel("Regular", MetricsIcons.REGULAR_COLOR, JLabel.CENTER);
        regularLabel.setBorder(headerBorder);
        JLabel highLabel = new JLabel("High", MetricsIcons.HIGH_COLOR, JLabel.CENTER);
        highLabel.setBorder(headerBorder);
        JLabel veryHighLabel = new JLabel("Very-high", MetricsIcons.VERY_HIGH_COLOR, JLabel.CENTER);
        veryHighLabel.setBorder(headerBorder);
        JLabel extremeLabel = new JLabel("Extreme", MetricsIcons.EXTREME_COLOR, JLabel.CENTER);
        extremeLabel.setBorder(headerBorder);

        TableCellRenderer renderer = new JComponentTableCellRenderer();

        table.getColumnModel().getColumn(2).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(3).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(4).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(5).setHeaderRenderer(renderer);

        table.getColumnModel().getColumn(2).setHeaderValue(regularLabel);
        table.getColumnModel().getColumn(3).setHeaderValue(highLabel);
        table.getColumnModel().getColumn(4).setHeaderValue(veryHighLabel);
        table.getColumnModel().getColumn(5).setHeaderValue(extremeLabel);

        panel = new JBScrollPane(table);
        Border b = IdeBorderFactory.createTitledBorder("Metrics value ranges");
        panel.setBorder(b);
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private List<MetricTypeRange> metricTypeRanges() {
        List<MetricTypeRange> result = new ArrayList<>();
        metricTypes.forEach(mt -> {
            Range range = MetricsService.getRangeForMetric(mt);
            MetricTypeRange metricTypeRange = new MetricTypeRange(mt, range);
            result.add(metricTypeRange);
        });
        return result;
    }

    private class Model extends AbstractTableModel {
        private List<MetricTypeRange> rows = metricTypeRanges();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 6;
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
                    return "Regular";
                case 3:
                    return "High";
                case 4:
                    return "Very-high";
                case 5:
                    return "Extreme";
                default:
                    return "";
            }
        }

        public void set(List<MetricTypeRange> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            MetricTypeRange metricTypeRange = rows.get(row);
            switch (column) {
                case 0:
                    return metricTypeRange.getMetricType();
                case 1:
                    return metricTypeRange.getMetricType().description();
                case 2:
                    return "[" + metricTypeRange.getRange().getRegularFrom() + ".."
                            + metricTypeRange.getRange().getRegularTo() +")";
                case 3:
                    return "[" + metricTypeRange.getRange().getHighFrom() + ".."
                            + metricTypeRange.getRange().getHighTo() +")";
                case 4:
                    return "[" + metricTypeRange.getRange().getVeryHighFrom() + ".."
                            + metricTypeRange.getRange().getVeryHighTo() +")";
                case 5:
                    return "[" + metricTypeRange.getRange().getExtremeFrom() + "..\u221E)";
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return MetricType.class;
            }
            return String.class;
        }
    }
    private static class MetricTypeRange {
        private final MetricType metricType;
        private final Range range;

        public MetricTypeRange(MetricType metricType, Range range) {
            this.metricType = metricType;
            this.range = range;
        }

        public MetricType getMetricType() {
            return metricType;
        }

        public Range getRange() {
            return range;
        }
    }

    private static class JComponentTableCellRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return (JComponent) value;
        }
    }
}
