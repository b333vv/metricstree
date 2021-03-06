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

package org.b333vv.metric.ui.settings.composition;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MetricsTreeSettingsTable {
    private final JPanel panel;
    private final Model model;

    public MetricsTreeSettingsTable(String emptyLabel,
                                    List<MetricsTreeSettingsStub> rows) {
        model = new Model();
        model.setRows(rows);

        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText(emptyLabel);
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);


        table.getTableHeader().setReorderingAllowed(true);

        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(300);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(250);

        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel = new JPanel(new BorderLayout());
        JScrollPane jScrollPane = new JBScrollPane(table);
        Border b = IdeBorderFactory.createTitledBorder("These class level and method level metrics should be calculated");
        jScrollPane.setBorder(b);
        panel.add(jScrollPane);
    }

    public JPanel getComponent() {
        return panel;
    }

    public void set(List<MetricsTreeSettingsStub> data) {
        model.set(data);
    }

    public List<MetricsTreeSettingsStub> get() {
        return new ArrayList<>(model.items());
    }

    private static class Model extends AbstractTableModel {
        private static final int COLUMN_COUNT = 5;
        private List<MetricsTreeSettingsStub> rows;

        public void setRows(List<MetricsTreeSettingsStub> rows) {
            this.rows = rows;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            rows.get(rowIndex).setNeedToConsider((Boolean)aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 1:
                    return "Metric";
                case 2:
                    return "Metrics Description";
                case 3:
                    return "Metrics Level";
                case 4:
                    return "Metrics Set";
                default:
                    return "";
            }
        }

        public void set(List<MetricsTreeSettingsStub> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public List<MetricsTreeSettingsStub> items() {
            return rows;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MetricsTreeSettingsStub item = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.isNeedToConsider();
                case 1:
                    return item.getType().name();
                case 2:
                    return item.getType().description();
                case 3:
                    return item.getType().level().level();
                case 4:
                    return item.getType().set().set();
                default:
                    return item;
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }
    }
}
