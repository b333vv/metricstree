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
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.MetricsIcons;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PackageLevelFitnessFunctionList {
    private final Model model;
    private final JBScrollPane panel;
    private final JBTable table;
    private Map<FitnessFunction, Integer> packagesCount;
    private final Project project;

    public PackageLevelFitnessFunctionList(Project project) {
        this.project = project;
        model = new Model();
        table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(15);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (table.getSelectedRow() >= 0) {
                Object selectedCell = table.getValueAt(table.getSelectedRow(), 1);
                FitnessFunction fitnessFunction = (FitnessFunction) selectedCell;
                this.project.getMessageBus()
                        .syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionSelected(fitnessFunction);
            }
        });

        panel = new JBScrollPane(table);
    }

    public void hideColumn(int index) {
        table.getColumnModel().getColumn(index).setWidth(0);
        table.getColumnModel().getColumn(index).setMinWidth(0);
        table.getColumnModel().getColumn(index).setMaxWidth(0);
    }

    public void setBorder(String text) {
        panel.setBorder(IdeBorderFactory.createTitledBorder(text));
    }

    public void setProfiles(Map<FitnessFunction, Set<PackageElement>> distribution) {
        ArrayList<FitnessFunction> rows = new ArrayList<>(distribution.keySet());
        packagesCount = distribution.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().size()));
        model.set(rows);
        model.fireTableDataChanged();
    }

    public JBScrollPane getComponent() {
        return panel;
    }

    public void clear() {
        model.set(List.of());
    }

    private class Model extends AbstractTableModel {
        private List<FitnessFunction> rows = List.of();

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
                    return "";
                case 1:
                    return "Fitness Function";
                case 2:
                    return "Formula";
                default:
                    return "";
            }
        }

        public void set(List<FitnessFunction> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            FitnessFunction fitnessFunction = rows.get(row);
            switch (column) {
                case 0:
                    return getImageForRow(fitnessFunction);
                case 1:
                    return fitnessFunction;
                case 2:
                    return getProfileStructure(fitnessFunction.profile());
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Icon.class;
                case 1:
                    return FitnessFunction.class;
                default:
                    return String.class;
            }
        }

        private String getProfileStructure(Map<MetricType, Range> structure) {
            return structure.entrySet().stream()
                    .map(this::buildProfileStructurePart)
                    .sorted()
                    .collect(Collectors.joining(" \u2227 "));
        }

        private String buildProfileStructurePart(Map.Entry<MetricType, Range> profile) {
            if (profile.getValue().getRegularFrom().equals(profile.getValue().getRegularTo())) {
                return profile.getKey() + " = " + profile.getValue().getRegularTo();
            }
            if (profile.getValue().getRegularTo().equals(Value.of(Long.MAX_VALUE))) {
                return profile.getKey() + " \u2265 " + profile.getValue().getRegularFrom();
            }
            if (profile.getValue().getRegularTo().equals(Value.of(Double.valueOf(Long.MAX_VALUE)))) {
                return profile.getKey() + " \u2265 " + profile.getValue().getRegularFrom();
            }
            if (profile.getValue().getRegularFrom().equals(Value.of(0L))) {
                return profile.getKey() + " < " + profile.getValue().getRegularTo();
            }
            if (profile.getValue().getRegularFrom().toString().equals("0,0")) {
                return profile.getKey() + " < " + profile.getValue().getRegularTo();
            }
            return profile.getKey() + " \u2208 [" + profile.getValue().getRegularFrom() + ".." + profile.getValue().getRegularTo() + ")";
        }
    }

    private Icon getImageForRow(FitnessFunction profile) {
        int count = packagesCount.get(profile);
        if (count > 0 && count <= 10) {
            return MetricsIcons.HIGH_COLOR;
        }
        if (count > 10 && count <= 20) {
            return MetricsIcons.VERY_HIGH_COLOR;
        }
        if (count > 20) {
            return MetricsIcons.EXTREME_COLOR;
        }
        return MetricsIcons.REGULAR_COLOR;
    }
}
