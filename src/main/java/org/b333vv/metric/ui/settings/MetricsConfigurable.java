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
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.fitnessfunction.PackageLevelFitnessFunctions;
import org.b333vv.metric.ui.settings.other.CalculationEngine;
import org.b333vv.metric.ui.settings.other.OtherSettings;
import org.b333vv.metric.ui.settings.fitnessfunction.ClassLevelFitnessFunctions;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.service.CacheService;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nullable;
import javax.swing.*;

public class MetricsConfigurable implements Configurable, Configurable.NoMargin, Configurable.NoScroll {
    private final Project project;
    private final BasicMetricsValidRangesSettings basicMetricsValidRangesSettings;
    private final DerivativeMetricsValidRangesSettings derivativeMetricsValidRangesSettings;
    private final ClassMetricsTreeSettings classMetricsTreeSettings;
    private final ClassLevelFitnessFunctions classLevelFitnessFunctions;
    private final PackageLevelFitnessFunctions packageLevelFitnessFunctions;
    private final OtherSettings otherSettings;
    private final CacheService cacheService;

    private SettingsPanel panel;

    public MetricsConfigurable(Project project) {
        this.project = project;
        this.cacheService = project.getService(CacheService.class);
        SettingsService settingsService = project.getService(SettingsService.class);
        this.basicMetricsValidRangesSettings = settingsService.getBasicMetricsSettings();
        this.derivativeMetricsValidRangesSettings = settingsService.getDerivativeMetricsSettings();
        this.classMetricsTreeSettings = settingsService.getClassMetricsTreeSettings();
        this.classLevelFitnessFunctions = settingsService.getClassLevelFitnessFunctions();
        this.packageLevelFitnessFunctions = settingsService.getPackageLevelFitnessFunctions();
        this.otherSettings = settingsService.getOtherSettings();
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
        return panel != null && (panel.isModified(basicMetricsValidRangesSettings)
                || panel.isModified(derivativeMetricsValidRangesSettings)
                || panel.isModified(classMetricsTreeSettings)
                || panel.isModified(classLevelFitnessFunctions)
                || panel.isModified(packageLevelFitnessFunctions)
                || panel.isModified(otherSettings));
    }

    @Override
    public void apply() {
        if (panel != null) {
            CalculationEngine oldCalculationEngine = otherSettings.getCalculationEngine();

            panel.save(basicMetricsValidRangesSettings);
            panel.save(derivativeMetricsValidRangesSettings);
            basicMetricsValidRangesSettings.clearTemporaryControlledMetrics();
            basicMetricsValidRangesSettings.clearTemporaryUnControlledMetrics();
            panel.save(classMetricsTreeSettings);
            panel.save(classLevelFitnessFunctions);
            panel.save(packageLevelFitnessFunctions);
            panel.save(otherSettings);

            if (oldCalculationEngine != otherSettings.getCalculationEngine()) {
                cacheService.invalidateUserData();
            }
        }
    }

    @Override
    public void reset() {
        basicMetricsValidRangesSettings.returnAllToUnControlledMetrics();
        basicMetricsValidRangesSettings.returnAllToControlledMetrics();
    }

    @Override
    public void disposeUIResources() {}
}
