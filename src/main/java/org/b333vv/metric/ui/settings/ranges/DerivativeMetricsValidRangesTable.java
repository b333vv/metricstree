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

package org.b333vv.metric.ui.settings.ranges;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DerivativeMetricsValidRangesTable {
    private final Function<DerivativeMetricsValidRangeStub, DerivativeMetricsValidRangeStub> onEdit;
    private final Supplier<DerivativeMetricsValidRangeStub> onAdd;
    private final JBTable table;
    private final JPanel panel;
    private final Model model;
    private final Project project;

    public DerivativeMetricsValidRangesTable(String emptyLabel,
                                             Function<DerivativeMetricsValidRangeStub,
                                                  DerivativeMetricsValidRangeStub> onEdit,
                                             Supplier<DerivativeMetricsValidRangeStub> onAdd,
                                             Project project) {
        this.project = project;
        this.onEdit = onEdit;
        this.onAdd = onAdd;

        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText(emptyLabel);
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);

        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(300);
        table.getColumnModel().getColumn(2).setMaxWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editEntry();
                }
            }
        });

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table)
                .setAddActionName("Add")
                .setAddAction(e -> addEntry())
                .setEditActionName("Edit")
                .setEditAction(e -> editEntry())
                .setRemoveActionName("Remove")
                .setRemoveAction(e -> removeEntry())
                .disableUpDownActions();

        panel = new JPanel(new BorderLayout());
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
    }

    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container)component, enable);
            }
        }
    }

    public JPanel getComponent() {
        return panel;
    }

    public void set(List<DerivativeMetricsValidRangeStub> data) {
        model.set(data);
    }

    public List<DerivativeMetricsValidRangeStub> get() {
        return new ArrayList<>(model.items());
    }

    private void editEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            DerivativeMetricsValidRangeStub value = model.items().get(selectedIndex);
            DerivativeMetricsValidRangeStub newValue = onEdit.apply(value);
            if (newValue != null) {
                model.items().set(selectedIndex, newValue);
            }
        }
    }

    private void addEntry() {
        DerivativeMetricsValidRangeStub newValue = onAdd.get();
        if (newValue != null) {
            model.items().add(newValue);
        }
    }

    private void removeEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            DerivativeMetricsValidRangeStub value = model.items().get(selectedIndex);
            DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings =
                    MetricsUtils.get(DerivativeMetricsValidRangesTable.this.project, DerivativeMetricsValidRangesSettings.class);
            derivativeMetricsValidRangesSettings.addToUnControlledMetrics(value);
            model.items().remove(value);
            panel.revalidate();
            panel.repaint();
        }
    }

    private class Model extends AbstractTableModel {
        private static final int COLUMN_COUNT = 5;
        final DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings =
                MetricsUtils.get(DerivativeMetricsValidRangesTable.this.project, DerivativeMetricsValidRangesSettings.class);
        private List<DerivativeMetricsValidRangeStub> rows = derivativeMetricsValidRangesSettings.getControlledMetricsList();

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
            return false;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Metric";
                case 1:
                    return "Description";
                case 2:
                    return "Level";
                case 3:
                    return "Min value";
                case 4:
                    return "Max value";
                default:
                    return "";
            }
        }

        public void set(List<DerivativeMetricsValidRangeStub> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public List<DerivativeMetricsValidRangeStub> items() {
            return rows;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DerivativeMetricsValidRangeStub item = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getName();
                case 1:
                    return item.getDescription();
                case 2:
                    return item.getLevel();
                case 3:
                    return item.getMinValue();
                case 4:
                    return item.getMaxValue();
                default:
                    return item;
            }
        }
    }
}
