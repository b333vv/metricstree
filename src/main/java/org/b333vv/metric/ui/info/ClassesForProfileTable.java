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

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.util.EditorUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassesForProfileTable {
    private final Model model;
    private final JBScrollPane panel;
    private Map<ClassElement, List<Metric>> classes;
    private final Project project;

    public ClassesForProfileTable(Map<ClassElement, List<Metric>> classesMap, Project project) {
        this.project = project;
        this.classes = classesMap.entrySet().stream()
                .filter(b -> !b.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        int columns = classes.entrySet().stream()
                .findFirst().get().getValue().size() + 1;

        model = new Model();
        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);

        table.setDefaultRenderer(Object.class, new CellRenderer());

        table.getSelectionModel().addListSelectionListener(event -> {
            if (table.getSelectedRow() >= 0) {
                Object selectedCell = table.getValueAt(table.getSelectedRow(), 0);
                ClassElement javaClass = (ClassElement) selectedCell;
                EditorUtils.openInEditor(project, javaClass.getPsiClass());
            }
        });
        panel = new JBScrollPane(table);
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    private class Model extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return classes.size();
        }

        @Override
        public int getColumnCount() {
            return classes.entrySet().stream()
                    .findFirst().get().getValue().size() + 1;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Class";
            }
            return classes.entrySet().stream()
                    .findFirst().get().getValue().get(column - 1).getType().name();
        }

        @Override
        public Object getValueAt(int row, int column) {
            // Use a list of entries to maintain consistency between keys and values
            List<Map.Entry<ClassElement, List<Metric>>> entries = new ArrayList<>(classes.entrySet());

            if (row >= entries.size()) {
                return null;
            }

            Map.Entry<ClassElement, List<Metric>> entry = entries.get(row);

            if (column == 0) {
                return entry.getKey();
            }

            List<Metric> metrics = entry.getValue();
            int metricIndex = column - 1;

            // Bounds check to prevent IndexOutOfBoundsException
            if (metricIndex >= 0 && metricIndex < metrics.size()) {
                return metrics.get(metricIndex);
            }

            return null;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return ClassElement.class;
            }
            return Metric.class;
        }
    }

    public class CellRenderer extends DefaultTableCellRenderer {
        private Color regularColor = new JBColor(new Color(0x499C54), new Color(0x499C54));
        private Color highColor = new JBColor(new Color(0xf9c784), new Color(0xf9c784));
        private Color veryHighColor = new JBColor(new Color(0xfc7a1e), new Color(0xfc7a1e));
        private Color extremeColor = new JBColor(new Color(0xf24c00), new Color(0xf24c00));

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Metric) {
                cell.setForeground(UIUtil.getPanelBackground());
                Metric metric = (Metric) value;
                setHorizontalAlignment(SwingConstants.CENTER);
                if (project.getService(SettingsService.class).getRangeForMetric(metric.getType())
                        .getRangeType(metric.getPsiValue()) == RangeType.REGULAR) {
                    cell.setBackground(regularColor);
                } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType())
                        .getRangeType(metric.getPsiValue()) == RangeType.HIGH) {
                    cell.setBackground(highColor);
                } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType())
                        .getRangeType(metric.getPsiValue()) == RangeType.VERY_HIGH) {
                    cell.setBackground(veryHighColor);
                } else if (project.getService(SettingsService.class).getRangeForMetric(metric.getType())
                        .getRangeType(metric.getPsiValue()) == RangeType.EXTREME) {
                    cell.setBackground(extremeColor);
                }
            } else {
                cell.setForeground(EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground());
                cell.setBackground(UIUtil.getPanelBackground());
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            setFont(table.getFont());
            setValue(value);
            return cell;
        }

        protected void setValue(Object value) {
            if (value instanceof Metric) {
                setText(((Metric) value).getPsiValue().toString());
            } else if (value instanceof ClassElement) {
                setText(((ClassElement) value).getName());
            } else {
                setText("");
            }
        }
    }
}
