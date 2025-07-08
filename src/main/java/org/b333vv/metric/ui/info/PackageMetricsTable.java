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

import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.MetricsIcons;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.SettingsService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PackageMetricsTable {
    private static final DecimalFormat METRIC_VALUE_FORMAT = new DecimalFormat("0.0###");
    private static final double EPSILON = 0.01;
    private final Model model;
    private final JBScrollPane panel;
    private final Map<String, Double> instability, abstractness;
    private JBTable table;
    private final Project project;

    public PackageMetricsTable(Map<String, Double> instability, Map<String, Double> abstractness, Project project) {
        this.project = project;
        this.instability = instability != null ? instability : new TreeMap<>();
        this.abstractness = abstractness != null ? abstractness : new TreeMap<>();

        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);

        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        table.getColumnModel().getColumn(0).setMaxWidth(15);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        panel = new JBScrollPane(table);
        Border b = IdeBorderFactory
                .createTitledBorder("Package Level Metrics Values: Instability, Abstractness, Normalized Distance From Main Sequence");
        panel.setBorder(b);
    }


    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private List<PackageMetricValue> projectMetricValues() {
        List<PackageMetricValue> result = new ArrayList<>();
        instability.forEach((key, value) -> {
            PackageMetricValue packageMetricValue = new PackageMetricValue(key, value,
                    abstractness.get(key));
            result.add(packageMetricValue);
        });
        return result;
    }

    public void updateSelection(double chartX, double chartY) {
        table.clearSelection();
        int i = 0;
        for (PackageMetricValue packageMetricValue : projectMetricValues()) {
            if (equals(packageMetricValue.getInstability(), chartX) && equals(packageMetricValue.getAbstractness(), chartY)) {
                table.getSelectionModel().addSelectionInterval(i, i);
            }
            i++;
        }
    }

    public boolean equals(double a, double b){
        return a == b || Math.abs(a - b) < EPSILON;
    }

    private class Model extends AbstractTableModel {
        private List<PackageMetricValue> rows = projectMetricValues();

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
                    return "Package";
                case 2:
                    return "Instability";
                case 3:
                    return "Abstractness";
                case 4:
                    return "Distance";
                default:
                    return "";
            }
        }

        public void set(List<PackageMetricValue> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            PackageMetricValue packageMetricValue = rows.get(row);
            switch (column) {
                case 0:
                    return getImageForRow(packageMetricValue.distance);
                case 1:
                    return packageMetricValue.getPackageName();
                case 2:
                    return METRIC_VALUE_FORMAT.format(packageMetricValue.getInstability());
                case 3:
                    return METRIC_VALUE_FORMAT.format(packageMetricValue.getAbstractness());
                case 4:
                    return METRIC_VALUE_FORMAT.format(packageMetricValue.getDistance());
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

        private Icon getImageForRow(double distance) {
            if (project.getService(SettingsService.class).getRangeForMetric(MetricType.D)
                    .getRangeType(Value.of(distance)) == RangeType.REGULAR) {
                return MetricsIcons.REGULAR_COLOR;
            }
            return MetricsIcons.EXTREME_COLOR;
        }
    }

    private static class PackageMetricValue {
        private final String packageName;
        private final Double instability;
        private final Double abstractness;
        private final Double distance;

        public PackageMetricValue(String packageName, Double instability, Double abstractness) {
            this.packageName = packageName;
            this.instability = instability;
            this.abstractness = abstractness;
            this.distance = Math.abs(1.0 - instability - abstractness);
        }

        public String getPackageName() {
            return packageName;
        }

        public Double getInstability() {
            return instability;
        }

        public Double getAbstractness() {
            return abstractness;
        }

        public Double getDistance() {
            return distance;
        }
    }
}
