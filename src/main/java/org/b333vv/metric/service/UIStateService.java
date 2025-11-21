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

package org.b333vv.metric.service;

import com.intellij.openapi.components.Service;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;

/**
 * Service for managing UI-related state in the MetricsTree plugin.
 * This service replaces static state management previously handled by
 * MetricsUtils.
 */
@Service
public final class UIStateService {
    private final MetricsTreeFilter classMetricsTreeFilter = new MetricsTreeFilter();
    private final MetricsTreeFilter projectMetricsTreeFilter = new MetricsTreeFilter();

    private boolean projectAutoScrollable = true;
    private boolean profileAutoScrollable = true;
    private boolean classMetricsTreeExists = true;
    private boolean projectTreeActive = false;
    private boolean classMetricsValuesEvolutionCalculationPerforming = false;
    private boolean classMetricsValuesEvolutionAdded = false;
    private com.intellij.openapi.module.Module selectedModule;

    /**
     * Returns the filter for class metrics tree.
     *
     * @return the class metrics tree filter
     */
    public MetricsTreeFilter getClassMetricsTreeFilter() {
        return classMetricsTreeFilter;
    }

    /**
     * Returns the filter for project metrics tree.
     *
     * @return the project metrics tree filter
     */
    public MetricsTreeFilter getProjectMetricsTreeFilter() {
        return projectMetricsTreeFilter;
    }

    /**
     * Checks if project tree is auto-scrollable.
     *
     * @return true if project tree is auto-scrollable, false otherwise
     */
    public boolean isProjectAutoScrollable() {
        return projectAutoScrollable;
    }

    /**
     * Sets whether project tree is auto-scrollable.
     *
     * @param value true to make project tree auto-scrollable, false otherwise
     */
    public void setProjectAutoScrollable(boolean value) {
        projectAutoScrollable = value;
    }

    /**
     * Checks if profile tree is auto-scrollable.
     *
     * @return true if profile tree is auto-scrollable, false otherwise
     */
    public boolean isProfileAutoScrollable() {
        return profileAutoScrollable;
    }

    /**
     * Sets whether profile tree is auto-scrollable.
     *
     * @param value true to make profile tree auto-scrollable, false otherwise
     */
    public void setProfileAutoScrollable(boolean value) {
        profileAutoScrollable = value;
    }

    /**
     * Checks if class metrics tree exists.
     *
     * @return true if class metrics tree exists, false otherwise
     */
    public boolean isClassMetricsTreeExists() {
        return classMetricsTreeExists;
    }

    /**
     * Sets whether class metrics tree exists.
     *
     * @param value true if class metrics tree exists, false otherwise
     */
    public void setClassMetricsTreeExists(boolean value) {
        classMetricsTreeExists = value;
    }

    /**
     * Checks if class metrics values evolution calculation is performing.
     *
     * @return true if calculation is performing, false otherwise
     */
    public boolean isClassMetricsValuesEvolutionCalculationPerforming() {
        return classMetricsValuesEvolutionCalculationPerforming;
    }

    /**
     * Sets whether class metrics values evolution calculation is performing.
     *
     * @param value true if calculation is performing, false otherwise
     */
    public void setClassMetricsValuesEvolutionCalculationPerforming(boolean value) {
        classMetricsValuesEvolutionCalculationPerforming = value;
    }

    /**
     * Checks if class metrics values evolution is added.
     *
     * @return true if class metrics values evolution is added, false otherwise
     */
    public boolean isClassMetricsValuesEvolutionAdded() {
        return classMetricsValuesEvolutionAdded;
    }

    /**
     * Sets whether class metrics values evolution is added.
     *
     * @param value true if class metrics values evolution is added, false otherwise
     */
    public void setClassMetricsValuesEvolutionAdded(boolean value) {
        classMetricsValuesEvolutionAdded = value;
    }

    /**
     * Checks if project tree is active.
     *
     * @return true if project tree is active, false otherwise
     */
    public boolean isProjectTreeActive() {
        return projectTreeActive;
    }

    /**
     * Sets whether project tree is active.
     *
     * @param value true if project tree is active, false otherwise
     * @return the new value
     */
    public boolean setProjectTreeActive(boolean value) {
        return projectTreeActive = value;
    }

    /**
     * Returns the selected module.
     *
     * @return the selected module, or null if no module is selected (project scope)
     */
    public com.intellij.openapi.module.Module getSelectedModule() {
        return selectedModule;
    }

    /**
     * Sets the selected module.
     *
     * @param selectedModule the module to select, or null for project scope
     */
    public void setSelectedModule(com.intellij.openapi.module.Module selectedModule) {
        this.selectedModule = selectedModule;
    }
}