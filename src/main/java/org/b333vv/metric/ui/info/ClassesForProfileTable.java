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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.util.EditorController;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassesForProfileTable {
    private final Model model;
    private final JBScrollPane panel;
    private Map<JavaClass, List<Metric>> classes;

    public ClassesForProfileTable(Map<JavaClass, List<Metric>> classesMap) {
        this.classes = classesMap;

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
                JavaClass javaClass = (JavaClass) selectedCell;
                MetricsUtils.openInEditor(javaClass.getPsiClass());
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
            List<JavaClass> javaClasses = new ArrayList<>(classes.keySet());
            if (column == 0) {
                return javaClasses.get(row);
            }
            List<List<Metric>> metrics = new ArrayList<>(classes.values());
            return metrics.get(row).get(column - 1);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return JavaClass.class;
            }
            return Metric.class;
        }
    }

    public static class CellRenderer extends DefaultTableCellRenderer {
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
                if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.REGULAR) {
                    cell.setBackground(regularColor);
                } else if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.HIGH) {
                    cell.setBackground(highColor);
                } else if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.VERY_HIGH) {
                    cell.setBackground(veryHighColor);
                } else if (MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.EXTREME) {
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
                setText(((Metric) value).getValue().toString());
            } else {
                setText(((JavaClass) value).getName());
            }
        }
    }
}
