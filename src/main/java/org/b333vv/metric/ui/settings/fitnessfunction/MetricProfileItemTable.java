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

package org.b333vv.metric.ui.settings.fitnessfunction;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MetricProfileItemTable {
    private final Model model;
    private final JPanel panel;
    private final Consumer<FitnessFunctionItem> onAdd;
    private final Consumer<FitnessFunctionItem> onEdit;
    private final Consumer<FitnessFunctionItem> onRemove;
    private final Supplier<ArrayList<String>> getMetricTypeList;
    private final JBTable table;
    private FitnessFunctionItem currentProfile;

    public MetricProfileItemTable(Consumer<FitnessFunctionItem> onAdd, Consumer<FitnessFunctionItem> onEdit,
                                  Consumer<FitnessFunctionItem> onRemove, Supplier<ArrayList<String>> getMetricTypeList) {
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onRemove = onRemove;
        this.getMetricTypeList = getMetricTypeList;
        model = new Model();
        table = new JBTable(model);
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
                currentProfile = (FitnessFunctionItem) selectedCell;
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

    public void setProfileItems(List<FitnessFunctionItem> profiles) {
        model.set(profiles);
//        model.fireTableDataChanged();
    }

    public JPanel getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private void addEntry() {
        AddMetricProfileItemDialog dialog = new AddMetricProfileItemDialog(MetricsUtils.getCurrentProject(), getMetricTypeList);
        if (dialog.showAndGet()) {
            FitnessFunctionItem item = dialog.getMetricProfileItem();
            if (item != null) {
                onAdd.accept(item);
            }
        }
    }

    private void editEntry() {
        EditMetricProfileItemDialog dialog = new EditMetricProfileItemDialog(MetricsUtils.getCurrentProject(), currentProfile);
        if (dialog.showAndGet()) {
            FitnessFunctionItem item = dialog.getMetricProfileItem();
            if (item != null) {
                onEdit.accept(dialog.getMetricProfileItem());
            }
        }
    }

    private void removeEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            FitnessFunctionItem value = model.rows.get(selectedIndex);
            onRemove.accept(value);
            model.fireTableDataChanged();
        }
    }

    private static class Model extends AbstractTableModel {
        private List<FitnessFunctionItem> rows = List.of();

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
                    return "Metric";
                case 1:
                    return "Min Value";
                case 2:
                    return "Max Value";
                default:
                    return "";
            }
        }

        public void set(List<FitnessFunctionItem> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            FitnessFunctionItem profile = rows.get(row);
            switch (column) {
                case 0:
                    return profile;
                case 1:
                    return profile.isLong() ? String.valueOf(profile.getMinLongValue())
                            : String.valueOf(profile.getMinDoubleValue());
                case 2:
                    return profile.isLong() ?
                            profile.getMaxLongValue() == Long.MAX_VALUE ? "\u221E" : String.valueOf(profile.getMaxLongValue()) :
                            profile.getMaxDoubleValue() == Long.MAX_VALUE ? "\u221E" : String.valueOf(profile.getMaxDoubleValue());
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return FitnessFunctionItem.class;
            }
            return String.class;
        }
    }
}
