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

package org.b333vv.metric.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings1;
import org.b333vv.metric.ui.settings.other.OtherSettings1;
import org.b333vv.metric.ui.settings.profile.MetricProfileSettings1;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings1;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings1;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nullable;
import javax.swing.*;

public class MetricsConfigurable implements Configurable, Configurable.NoMargin, Configurable.NoScroll {
    private final Project project;
    private final BasicMetricsValidRangesSettings1 basicMetricsValidRangesSettings1;
    private final DerivativeMetricsValidRangesSettings1 derivativeMetricsValidRangesSettings1;
    private final ClassMetricsTreeSettings1 classMetricsTreeSettings1;
    private final MetricProfileSettings1 metricProfileSettings1;
    private final OtherSettings1 otherSettings1;

    private SettingsPanel panel;

    public MetricsConfigurable(Project project) {
        this.project = project;
//        this.basicMetricsValidRangesSettings = project.getComponent(BasicMetricsValidRangesSettings.class);
        this.basicMetricsValidRangesSettings1 = project.getService(BasicMetricsValidRangesSettings1.class);
//        this.derivativeMetricsValidRangesSettings = project.getComponent(DerivativeMetricsValidRangesSettings.class);
        this.derivativeMetricsValidRangesSettings1 = project.getService(DerivativeMetricsValidRangesSettings1.class);
//        this.classMetricsTreeSettings = project.getComponent(ClassMetricsTreeSettings.class);
        this.classMetricsTreeSettings1 = project.getService(ClassMetricsTreeSettings1.class);
//        this.metricProfileSettings = project.getComponent(MetricProfileSettings.class);
        this.metricProfileSettings1 = project.getService(MetricProfileSettings1.class);
//        this.otherSettings = project.getComponent(OtherSettings.class);
        this.otherSettings1 = project.getService(OtherSettings1.class);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "MetricsTree";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (panel == null) {
            panel = new SettingsPanel(project);
        }
        return panel.getRootPane();
    }

    @Override
    public boolean isModified() {
        return panel != null && (panel.isModified(basicMetricsValidRangesSettings1)
                || panel.isModified(derivativeMetricsValidRangesSettings1)
                || panel.isModified(classMetricsTreeSettings1)
                || panel.isModified(metricProfileSettings1)
                || panel.isModified(otherSettings1));
    }

    @Override
    public void apply() {
        if (panel != null) {
            panel.save(basicMetricsValidRangesSettings1);
            panel.save(derivativeMetricsValidRangesSettings1);
            basicMetricsValidRangesSettings1.clearTemporaryControlledMetrics();
            basicMetricsValidRangesSettings1.clearTemporaryUnControlledMetrics();
            panel.save(classMetricsTreeSettings1);
            panel.save(metricProfileSettings1);
            panel.save(otherSettings1);
        }
    }

    @Override
    public void reset() {
        basicMetricsValidRangesSettings1.returnAllToUnControlledMetrics();
        basicMetricsValidRangesSettings1.returnAllToControlledMetrics();
    }

    @Override
    public void disposeUIResources() {}
}
