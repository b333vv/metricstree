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

import com.intellij.psi.PsiPackage;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ClassLevelFitnessFunctionClassTable {
    private final Model model;
    private final JBScrollPane panel;

    public ClassLevelFitnessFunctionClassTable() {
        model = new Model();
        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (table.getSelectedRow() >= 0) {
                Object selectedCell = table.getValueAt(table.getSelectedRow(), 0);
                JavaClass javaClass = (JavaClass) selectedCell;
                if (MetricsUtils.isProfileAutoScrollable()) {
                    MetricsUtils.openInEditor(javaClass.getPsiClass());
                }
                MetricsUtils.getCurrentProject().getMessageBus()
                        .syncPublisher(MetricsEventListener.TOPIC).javaClassSelected(javaClass);
            }
        });
        panel = new JBScrollPane(table);
    }

    public void setClasses(List<JavaClass> classes) {
        model.set(classes);
        model.fireTableDataChanged();
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private static class Model extends AbstractTableModel {
        private List<JavaClass> rows = List.of();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Class";
                case 1:
                    return "Package";
                default:
                    return "";
            }
        }

        public void set(List<JavaClass> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            JavaClass javaClass = rows.get(row);
            switch (column) {
                case 0:
                    return javaClass;
                case 1:
                    return getPackage(javaClass);
                default:
                    return "";
            }
        }

        private Object getPackage(JavaClass javaClass) {
            PsiPackage psiPackage = ClassUtils.findPackage(javaClass.getPsiClass());
            if (psiPackage == null) {
                return "";
            }
            return psiPackage.getQualifiedName();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return JavaClass.class;
            }
            return String.class;
        }
    }
}
