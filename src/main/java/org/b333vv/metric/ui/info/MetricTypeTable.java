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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.EditorController;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MetricTypeTable {
    private final Model model;
    private final JBScrollPane panel;
    private final List<MetricType> metricTypes;

    public MetricTypeTable(List<MetricType> metricTypes) {
        this.metricTypes = metricTypes;

        model = new Model();
        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setMaxWidth(50);

        table.getSelectionModel().addSelectionInterval(0, 0);

        table.getSelectionModel().addListSelectionListener(event -> {
            Object selectedCell = table.getValueAt(table.getSelectedRow(), 0);
            MetricType metricType = (MetricType) selectedCell;
            MetricsUtils.getCurrentProject()
                    .getMessageBus().syncPublisher(MetricsEventListener.TOPIC).currentMetricType(metricType);
        });

        panel = new JBScrollPane(table);
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private class Model extends AbstractTableModel {
        private List<MetricType> rows = metricTypes;

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
                    return "Name";
                case 1:
                    return "Description";
                case 2:
                    return "Set";
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
                    return metricType;
                case 1:
                    return metricType.description();
                case 2:
                    return metricType.set().set();
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
}
