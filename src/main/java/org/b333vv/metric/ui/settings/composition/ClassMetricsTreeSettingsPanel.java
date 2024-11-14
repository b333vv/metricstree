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

import org.b333vv.metric.ui.settings.ConfigurationPanel;

import javax.swing.*;
import java.util.List;

public class ClassMetricsTreeSettingsPanel implements ConfigurationPanel<ClassMetricsTreeSettings1> {
    private static final String EMPTY_LABEL = "No metrics configured";
    private JPanel panel;
    private MetricsTreeSettingsTable metricsTreeSettingsTable;

    public ClassMetricsTreeSettingsPanel(ClassMetricsTreeSettings1 classMetricsTreeSettings1) {
        createUIComponents(classMetricsTreeSettings1);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(ClassMetricsTreeSettings1 settings) {
        List<MetricsTreeSettingsStub> rows = settings.getMetricsList();
        return !rows.equals(metricsTreeSettingsTable.get());
    }

    @Override
    public void save(ClassMetricsTreeSettings1 settings) {
        settings.setClassTreeMetrics(metricsTreeSettingsTable.get());
    }

    @Override
    public void load(ClassMetricsTreeSettings1 settings) {
        metricsTreeSettingsTable.set(settings.getMetricsList());
    }

    private void createUIComponents(ClassMetricsTreeSettings1 classMetricsTreeSettings1) {
        metricsTreeSettingsTable = new MetricsTreeSettingsTable(EMPTY_LABEL, classMetricsTreeSettings1.getMetricsList());
        panel = metricsTreeSettingsTable.getComponent();
    }
}
