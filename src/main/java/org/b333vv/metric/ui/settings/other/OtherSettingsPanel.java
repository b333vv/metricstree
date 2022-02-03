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

package org.b333vv.metric.ui.settings.other;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import org.b333vv.metric.ui.settings.ConfigurationPanel;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;

public class OtherSettingsPanel implements ConfigurationPanel<OtherSettings> {
    private final Project project;
    private JPanel panel;
    private JCheckBox projectMetricsStampStored;

    public OtherSettingsPanel(Project project, OtherSettings settings) {
        this.project = project;
        createUIComponents(settings);
    }

    public JPanel getComponent() {
        return panel;
    }

    @Override
    public boolean isModified(OtherSettings settings) {
        return settings.isProjectMetricsStampStored() != projectMetricsStampStored.isSelected();
    }

    @Override
    public void save(OtherSettings settings) {
        settings.setProjectMetricsStampStored(projectMetricsStampStored.isSelected());
    }

    @Override
    public void load(OtherSettings settings) {
        projectMetricsStampStored.setSelected(settings.isProjectMetricsStampStored());;
    }

    private void createUIComponents(OtherSettings settings) {

        projectMetricsStampStored = new JCheckBox("Save values of project level metrics in files " +
                "on disk during their calculation",
                settings.isProjectMetricsStampStored());

        panel = new JPanel(new GridBagLayout());

        JBInsets insets = JBUI.insets(2, 2, 2, 2);

        panel.add(projectMetricsStampStored, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                NORTHWEST, NONE, insets, 0, 0));
    }
}
