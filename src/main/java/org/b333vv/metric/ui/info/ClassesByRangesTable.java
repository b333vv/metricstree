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
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.EditorController;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ClassesByRangesTable {
    private final Model model;
    private final JBScrollPane panel;
    private final List<ClassByRange> classByRanges;

    public ClassesByRangesTable(List<ClassByRange> classByRanges) {
        this.classByRanges = classByRanges;

        model = new Model();
        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(2).setMaxWidth(100);

        table.getSelectionModel().addListSelectionListener(event -> {
            Object selectedCell = table.getValueAt(table.getSelectedRow(), 0);
            JavaClass javaClass = (JavaClass) selectedCell;
            openInEditor(javaClass.getPsiClass());
        });

        panel = new JBScrollPane(table);
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private void openInEditor(PsiElement psiElement) {
        final EditorController caretMover = new EditorController(MetricsUtils.getCurrentProject());
        if (psiElement != null) {
            Editor editor = caretMover.openInEditor(psiElement);
            if (editor != null) {
                caretMover.moveEditorCaret(psiElement);
            }
        }
    }

    private class Model extends AbstractTableModel {
        private List<ClassByRange> rows = classByRanges;

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
                    return "Class";
                case 1:
                    return "Range";
                case 2:
                    return "Value";
                default:
                    return "";
            }
        }

        public void set(List<ClassByRange> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            ClassByRange classByRange = rows.get(row);
            switch (column) {
                case 0:
                    return classByRange.getJavaClass();
                case 1:
                    return classByRange.getRange();
                case 2:
                    return classByRange.getValue();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return JavaClass.class;
            }
            return String.class;
        }
    }

    public static class ClassByRange {
        private final JavaClass javaClass;
        private final String range;
        private final Value value;

        public ClassByRange(JavaClass javaClass, String range, Value value) {
            this.javaClass = javaClass;
            this.range = range;
            this.value = value;
        }

        public JavaClass getJavaClass() {
            return javaClass;
        }

        public String getRange() {
            return range;
        }

        public Value getValue() {
            return value;
        }
    }
}
