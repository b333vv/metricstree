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
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.util.MetricsService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.Collectors;

public class MetricTypeSelectorTable {
    private final Model model;
    private final JBScrollPane panel;
    private final Set<MetricType> metricTypes;
    private final Set<JavaClass> javaClasses;
    private final JBTable table;

    public MetricTypeSelectorTable(JavaProject javaProject, Consumer<MetricType> selectAction) {
        this.javaClasses = javaProject.allClasses().collect(Collectors.toUnmodifiableSet());
        this.metricTypes = Arrays.stream(MetricType.values())
                .filter(mt -> mt.level() == MetricLevel.CLASS)
                .sorted(Comparator.comparing(MetricType::description))
                .collect(Collectors.toCollection(LinkedHashSet::new));

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
        table.getColumnModel().getColumn(1).setWidth(0);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(2).setMaxWidth(250);

        table.getSelectionModel().addListSelectionListener(event -> {
            Object selectedCell = table.getValueAt(table.getSelectedRow(), 1);
            MetricType metricType = (MetricType) selectedCell;
            selectAction.consume(metricType);
        });

        panel = new JBScrollPane(table);
        panel.setBorder(IdeBorderFactory.createTitledBorder("Select Metric Type"));
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private class Model extends AbstractTableModel {
        private List<MetricType> rows = new ArrayList<>(metricTypes);

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
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
                    return "Metric";
                case 2:
                    return "Metric Description";
                default:
                    return "";
            }
        }

        public void set(List<MetricType> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            MetricType metricType = rows.get(row);
            switch (column) {
                case 0:
                    return getImageForRow(metricType);
                case 1:
                    return metricType;
                case 2:
                    return metricType.description();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return Icon.class;
            }
            if (column == 1) {
                return MetricType.class;
            }
            return String.class;
        }

        private Icon getImageForRow(MetricType metricType) {
            if (isInRange(metricType, RangeType.EXTREME)) {
                return MetricsIcons.EXTREME_COLOR;
            }
            if (isInRange(metricType, RangeType.VERY_HIGH)) {
                return MetricsIcons.VERY_HIGH_COLOR;
            }
            if (isInRange(metricType, RangeType.HIGH)) {
                return MetricsIcons.HIGH_COLOR;
            }
            if (isInRange(metricType, RangeType.REGULAR)) {
                return MetricsIcons.REGULAR_COLOR;
            }
            return MetricsIcons.NOT_TRACKED;
        }

        private boolean isInRange(MetricType metricType, RangeType rangeType) {
            return javaClasses.stream()
                    .map(javaClass -> javaClass.metric(metricType))
                    .filter(Objects::nonNull)
                    .anyMatch(metric -> project.getService(MetricsService.class).getRangeForMetric(metricType)
                            .getRangeType(metric.getValue()) == rangeType);
        }
    }
}
