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

package org.b333vv.metric.ui.fitnessfunction;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaPackage;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class PackageLevelFitnessFunctionPackageTable {
    private final Model model;
    private final JBScrollPane panel;
    private final Project project;

    public PackageLevelFitnessFunctionPackageTable(Project project) {
        this.project = project;
        model = new Model();
        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setCellRenderer(new PackageNameRenderer());

        table.getSelectionModel().addListSelectionListener(event -> {
            if (table.getSelectedRow() >= 0) {
                Object selectedCell = table.getValueAt(table.getSelectedRow(), 0);
                JavaPackage javaPackage = (JavaPackage) selectedCell;
                this.project.getMessageBus()
                        .syncPublisher(MetricsEventListener.TOPIC).javaPackageSelected(javaPackage);
            }
        });
        panel = new JBScrollPane(table);
    }

    public void setPackages(List<JavaPackage> packages) {
        model.set(packages);
        model.fireTableDataChanged();
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private static class Model extends AbstractTableModel {
        private List<JavaPackage> rows = List.of();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0, 1 -> "Package";
                default -> "";
            };
        }

        public void set(List<JavaPackage> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            JavaPackage javaPackage = rows.get(row);
            return switch (column) {
                case 0, 1 -> javaPackage;
                default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return JavaPackage.class;
        }
    }

    private static class PackageNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JavaPackage javaPackage) {
                value = javaPackage.getPsiPackage().getQualifiedName();
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
