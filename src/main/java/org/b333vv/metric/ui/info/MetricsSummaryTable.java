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
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.SettingsService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MetricsSummaryTable {
    private final Model model;
    private final JBScrollPane panel;
    private final JBTable table;
    private final boolean withBorder;
    private final Project project;

    public MetricsSummaryTable(boolean withBorder, Project project) {
        this.withBorder = withBorder;
        this.project = project;
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
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(250);
        table.getColumnModel().getColumn(4).setMaxWidth(130);
        table.getColumnModel().getColumn(5).setMaxWidth(200);

        panel = new JBScrollPane(table);
    }

    public void hideColumn(int index) {
        table.getColumnModel().getColumn(index).setWidth(0);
        table.getColumnModel().getColumn(index).setMinWidth(0);
        table.getColumnModel().getColumn(index).setMaxWidth(0);
    }

    private void hideOrShowValidValuesColumn(boolean controlValidRanges) {
        if (controlValidRanges) {
            table.getColumnModel().getColumn(0).setWidth(15);
            table.getColumnModel().getColumn(0).setMinWidth(15);
            table.getColumnModel().getColumn(0).setMaxWidth(15);

            table.getColumnModel().getColumn(5).setWidth(200);
            table.getColumnModel().getColumn(5).setMaxWidth(200);
        } else {
            table.getColumnModel().getColumn(0).setWidth(0);
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);

            table.getColumnModel().getColumn(5).setWidth(0);
            table.getColumnModel().getColumn(5).setMinWidth(0);
            table.getColumnModel().getColumn(5).setMaxWidth(0);
        }
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        if (withBorder) {
            panel.setBorder(IdeBorderFactory.createTitledBorder(""));
        }
        model.set(List.of());
    }

    public void set(JavaCode javaCode) {
        if (withBorder) {
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
        }
        List<Metric> sortedMetrics = javaCode.metrics()
                .sorted(Comparator.comparing(Metric::getType))
                .collect(Collectors.toList());
        model.set(sortedMetrics);
        hideOrShowValidValuesColumn(project.getService(SettingsService.class).isControlValidRanges());
        model.fireTableDataChanged();
    }

    private class Model extends AbstractTableModel {

        private List<Metric> rows = List.of();

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
                case 1:
                    return "Metric";
                case 2:
                    return "Metrics Set";
                case 3:
                    return "Description";
                case 4:
                    return "Value";
                case 5:
                    return "Regular Range";
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
                    return metric.getType().name();
                case 2:
                    return metric.getType().set().set();
                case 3:
                    return metric.getType().description();
                case 4: {
                    Value psiValue = metric.getPsiValue();
                    Value javaParserValue = metric.getJavaParserValue();

                    String psiValueString;
                    if (metric.getType().set() == MetricSet.MOOD) {
                        psiValueString = psiValue.percentageFormat();
                    } else {
                        psiValueString = psiValue.toString();
                    }

                    if (javaParserValue != null && javaParserValue != Value.UNDEFINED) {
                        String javaParserValueString;
                        if (metric.getType().set() == MetricSet.MOOD) {
                            javaParserValueString = javaParserValue.percentageFormat();
                        } else {
                            javaParserValueString = javaParserValue.toString();
                        }
                        return psiValueString + " (" + javaParserValueString + ")";
                    } else {
                        return psiValueString;
                    }
                }
                case 5:
                    if (metric.getType().set() == MetricSet.MOOD) {
                        return project.getService(SettingsService.class).getRangeForMetric(metric.getType()).percentageFormat();
                    }
                default:
                    return project.getService(SettingsService.class).getRangeForMetric(metric.getType());
            }
        }

        private Icon getRowIcon(Metric metric) {
            if (metric.getPsiValue() == Value.UNDEFINED) {
                return MetricsIcons.NA;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.REGULAR) {
                return MetricsIcons.REGULAR_COLOR;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.HIGH) {
                return MetricsIcons.HIGH_COLOR;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.VERY_HIGH) {
                return MetricsIcons.VERY_HIGH_COLOR;
            }
            if (project.getService(SettingsService.class).getRangeForMetric(metric.getType()).getRangeType(metric.getPsiValue()) == RangeType.EXTREME) {
                return MetricsIcons.EXTREME_COLOR;
            }
            return MetricsIcons.NOT_TRACKED;
        }
    }
}
