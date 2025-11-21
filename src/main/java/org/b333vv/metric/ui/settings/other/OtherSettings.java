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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(name = "OtherSettings", storages = { @Storage("other-settings.xml") })
public final class OtherSettings implements PersistentStateComponent<OtherSettings> {

    private boolean projectMetricsStampStored;
    private boolean includeTestFiles;
    private CalculationEngine calculationEngine = CalculationEngine.PSI;

    public OtherSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        projectMetricsStampStored = true;
        includeTestFiles = false;

    }

    public boolean isIncludeTestFiles() {
        return includeTestFiles;
    }

    public void setIncludeTestFiles(boolean includeTestFiles) {
        this.includeTestFiles = includeTestFiles;
    }

    public boolean isProjectMetricsStampStored() {
        return projectMetricsStampStored;
    }

    public void setProjectMetricsStampStored(boolean projectMetricsStampStored) {
        this.projectMetricsStampStored = projectMetricsStampStored;
    }

    public CalculationEngine getCalculationEngine() {
        return calculationEngine;
    }

    public void setCalculationEngine(CalculationEngine calculationEngine) {
        this.calculationEngine = calculationEngine;
    }

    @Override
    public synchronized OtherSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull OtherSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}