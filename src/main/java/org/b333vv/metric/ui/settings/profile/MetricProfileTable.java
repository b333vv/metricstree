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

package org.b333vv.metric.ui.settings.profile;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MetricProfileTable {
    private final Model model;
    private final JPanel panel;
    private final Function<Map.Entry<String, List<MetricProfileItem>>,
            Map.Entry<String, List<MetricProfileItem>>> onEdit;
    private final Supplier<Map.Entry<String, List<MetricProfileItem>>> onAdd;
    private Map<String, List<MetricProfileItem>> profiles;
    private JBTable table;

    public MetricProfileTable(Function<Map.Entry<String, List<MetricProfileItem>>,
            Map.Entry<String, List<MetricProfileItem>>> onEdit,
                              Supplier<Map.Entry<String, List<MetricProfileItem>>> onAdd) {
        model = new Model();
        this.onEdit = onEdit;
        this.onAdd = onAdd;
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);

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

    public void setProfiles(Map<String, List<MetricProfileItem>> profiles) {
        this.profiles = profiles;
        model.set(new ArrayList<>(profiles.keySet()));
        model.fireTableDataChanged();
    }

    public Map<String, List<MetricProfileItem>> getProfiles() {
        return profiles;
    }

    public JPanel getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private void addEntry() {
        Map.Entry<String, List<MetricProfileItem>> newEntry = onAdd.get();
        if (newEntry != null) {
            profiles.put(newEntry.getKey(), newEntry.getValue());
            model.rows.add(newEntry.getKey());
        }
    }

    private void editEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            String key = model.rows.get(selectedIndex);
            Map.Entry<String, List<MetricProfileItem>> entry = Map.entry(key, profiles.get(key));
            Map.Entry<String, List<MetricProfileItem>> newEntry = onEdit.apply(entry);
            if (newEntry != null) {
                profiles.remove(entry.getKey());
                profiles.put(newEntry.getKey(), newEntry.getValue());
                model.rows.set(selectedIndex, newEntry.getKey());
            }
        }
    }

    private void removeEntry() {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            String key = model.rows.get(selectedIndex);
            profiles.remove(key);
            model.rows.remove(selectedIndex);
            model.fireTableDataChanged();
        }
    }

    private class Model extends AbstractTableModel {
        private List<String> rows = List.of();

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
                    return "Name";
                case 1:
                    return "Metric Profile";
                default:
                    return "";
            }
        }

        public void set(List<String> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            String profileName = rows.get(row);
            switch (column) {
                case 0:
                    return profileName;
                case 1:
                    return getProfileStructure(profiles.get(profileName));
                default:
                    return "";
            }
        }

        private String getProfileStructure(List<MetricProfileItem> structureList) {
            return structureList.stream()
                    .map(this::buildProfileStructurePart)
                    .sorted()
                    .collect(Collectors.joining(" \u2227 "));
        }

        private String buildProfileStructurePart(MetricProfileItem m) {
            if (m.isLong()) {
                if (m.getMinLongValue() == m.getMaxLongValue()) {
                    return m.getName() + " = " + m.getMaxLongValue();
                }
                if (m.getMaxLongValue() == Long.MAX_VALUE) {
                    return m.getName() + " \u2265 " + m.getMinLongValue();
                }
                if (m.getMinLongValue() == 0L) {
                    return m.getName() + " < " + m.getMaxLongValue();
                }
                return m.getName() + " \u2208 [" + m.getMinLongValue() + ".." + m.getMaxLongValue() + ")";
            } else {
                if (m.getMinDoubleValue() == m.getMaxDoubleValue()) {
                    return m.getName() + " = " + m.getMaxDoubleValue();
                }
                if (m.getMaxDoubleValue() == Long.MAX_VALUE) {
                    return m.getName() + " \u2265 " + m.getMinDoubleValue();
                }
                if (m.getMinDoubleValue() == 0.0) {
                    return m.getName() + " < " + m.getMaxDoubleValue();
                }
                return m.getName() + " \u2208 [" + m.getMinDoubleValue() + ".." + m.getMaxDoubleValue() + ")";
            }
        }
    }
}
