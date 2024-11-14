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

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MetricByProfileTable {
    private final Model model;
    private final JBScrollPane panel;
    private List<MetricProfile> metricProfiles;

    public MetricByProfileTable(List<MetricProfile> metricProfiles) {
        this.metricProfiles = metricProfiles;
        model = new Model();
        JBTable table = new JBTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(JBUI.emptySize());
        table.getEmptyText().setText("");
        table.setDragEnabled(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getSelectionModel().addSelectionInterval(0, 0);

        table.getSelectionModel().addListSelectionListener(event -> {
            Object selectedCell = table.getValueAt(table.getSelectedRow(), 0);
            MetricProfile metricProfile = (MetricProfile) selectedCell;
            MetricsUtils.getCurrentProject()
                    .getMessageBus().syncPublisher(MetricsEventListener.TOPIC).currentMetricProfile(metricProfile);
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
        private List<MetricProfile> rows = metricProfiles;

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            return "Profile";
        }

        public void set(List<MetricProfile> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return rows.get(row);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return MetricProfile.class;
        }
    }
}
