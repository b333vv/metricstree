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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Long.MAX_VALUE;
import static org.b333vv.metric.model.metric.MetricType.*;

@State(
        name = "MetricProfileSettings",
        storages = {@Storage("metric-profile-settings.xml")}
)
public final class MetricProfileSettings1 implements PersistentStateComponent<MetricProfileSettings1> {

    private final Map<String, List<MetricProfileItem>> profiles = new TreeMap<>();

    public MetricProfileSettings1() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        // God Class (type 1)
        List<MetricProfileItem> godClass1 = new ArrayList<>();
        addMetricProfileItem(godClass1, WMC.name(), 47, MAX_VALUE);
        addMetricProfileItem(godClass1, ATFD.name(), 6, MAX_VALUE);
        addMetricProfileItem(godClass1, TCC.name(), 0.00, 0.33);
        profiles.put("God Class (type 1)", godClass1);

        // God Class (type 2)
        List<MetricProfileItem> godClass2 = new ArrayList<>();
        addMetricProfileItem(godClass2, WMC.name(), 44, MAX_VALUE);
        addMetricProfileItem(godClass2, ATFD.name(), 0, 4);
        addMetricProfileItem(godClass2, NOA.name(), 30, MAX_VALUE);
        profiles.put("God Class (type 2)", godClass2);

        // God Class (type 3)
        List<MetricProfileItem> godClass3 = new ArrayList<>();
        addMetricProfileItem(godClass3, WMC.name(), 44, MAX_VALUE);
        addMetricProfileItem(godClass3, ATFD.name(), 4, MAX_VALUE);
        addMetricProfileItem(godClass3, CBO.name(), 11, MAX_VALUE);
        profiles.put("God Class (type 3)", godClass3);

        // God Class (type 4)
        List<MetricProfileItem> godClass4 = new ArrayList<>();
        addMetricProfileItem(godClass4, WMC.name(), 46, MAX_VALUE);
        addMetricProfileItem(godClass4, TCC.name(), 0.00, 0.37);
        addMetricProfileItem(godClass4, CBO.name(), 1, MAX_VALUE);
        addMetricProfileItem(godClass4, RFC.name(), 144, MAX_VALUE);
        profiles.put("God Class (type 4)", godClass4);

        // High Coupling
        List<MetricProfileItem> highCoupling = new ArrayList<>();
        addMetricProfileItem(highCoupling, CBO.name(), 20, MAX_VALUE);
        profiles.put("High Coupling", highCoupling);

        // Long Parameters List
        List<MetricProfileItem> longParametersList = new ArrayList<>();
        addMetricProfileItem(longParametersList, NOPM.name(), 4, MAX_VALUE);
        profiles.put("Long Parameters List", longParametersList);

        // Long Method
        List<MetricProfileItem> longMethods = new ArrayList<>();
        addMetricProfileItem(longMethods, LOC.name(), 16, MAX_VALUE);
        profiles.put("Long Method", longMethods);

        // Complex Method
        List<MetricProfileItem> complexMethods = new ArrayList<>();
        addMetricProfileItem(complexMethods, CC.name(), 8, MAX_VALUE);
        profiles.put("Complex Method", complexMethods);

        // Feature Envy
        List<MetricProfileItem> featureEnvy = new ArrayList<>();
        addMetricProfileItem(featureEnvy, ATFD.name(), 5, MAX_VALUE);
        addMetricProfileItem(featureEnvy, FDP.name(), 5, MAX_VALUE);
        addMetricProfileItem(featureEnvy, LAA.name(), 0.00, 0.33);
        profiles.put("Feature Envy", featureEnvy);

        // Brain Method
        List<MetricProfileItem> brainMethod = new ArrayList<>();
        addMetricProfileItem(brainMethod, LOC.name(), 30, MAX_VALUE);
        addMetricProfileItem(brainMethod, CC.name(), 3, MAX_VALUE);
        addMetricProfileItem(brainMethod, MND.name(), 3, MAX_VALUE);
        addMetricProfileItem(brainMethod, NOAV.name(), 3, MAX_VALUE);
        profiles.put("Brain Method", brainMethod);

        // Brain Class
        List<MetricProfileItem> brainClass = new ArrayList<>();
        addMetricProfileItem(brainClass, LOC.name(), 30, MAX_VALUE);
        addMetricProfileItem(brainClass, CC.name(), 3, MAX_VALUE);
        addMetricProfileItem(brainClass, MND.name(), 3, MAX_VALUE);
        addMetricProfileItem(brainClass, NOAV.name(), 3, MAX_VALUE);
        addMetricProfileItem(brainClass, WMC.name(), 34, MAX_VALUE);
        addMetricProfileItem(brainClass, TCC.name(), 0.00, 0.50);
        profiles.put("Brain Class", brainClass);

        // Intensive Coupling
        List<MetricProfileItem> intensiveCoupling1 = new ArrayList<>();
        addMetricProfileItem(intensiveCoupling1, CINT.name(), 8, MAX_VALUE);
        addMetricProfileItem(intensiveCoupling1, CDISP.name(), 0.00, 0.50);
        addMetricProfileItem(intensiveCoupling1, MND.name(), 2, MAX_VALUE);
        profiles.put("Intensive Coupling", intensiveCoupling1);

        // Dispersed Coupling
        List<MetricProfileItem> dispersedCoupling = new ArrayList<>();
        addMetricProfileItem(dispersedCoupling, CINT.name(), 8, MAX_VALUE);
        addMetricProfileItem(dispersedCoupling, CDISP.name(), 0.66, MAX_VALUE);
        addMetricProfileItem(dispersedCoupling, MND.name(), 2, MAX_VALUE);
        profiles.put("Dispersed Coupling", dispersedCoupling);

        // Deeply Nested Conditions
        List<MetricProfileItem> deeplyNestedConditions = new ArrayList<>();
        addMetricProfileItem(deeplyNestedConditions, CND.name(), 3, MAX_VALUE);
        profiles.put("Deeply Nested Conditions", deeplyNestedConditions);

        // Too Many Fields
        List<MetricProfileItem> tooManyFields = new ArrayList<>();
        addMetricProfileItem(tooManyFields, NOA.name(), 15, MAX_VALUE);
        profiles.put("Too Many Fields", tooManyFields);

        // Too Many Methods
        List<MetricProfileItem> tooManyMethods = new ArrayList<>();
        addMetricProfileItem(tooManyMethods, NOM.name(), 10, MAX_VALUE);
        profiles.put("Too Many Methods", tooManyMethods);

        // Data Class
        List<MetricProfileItem> dataClass = new ArrayList<>();
        addMetricProfileItem(dataClass, WMC.name(), 0, 15);
        addMetricProfileItem(dataClass, WOC.name(), 0.00, 0.34);
        addMetricProfileItem(dataClass, NOAM.name(), 4, MAX_VALUE);
        addMetricProfileItem(dataClass, NOPA.name(), 3, MAX_VALUE);
        profiles.put("Data Class", dataClass);

    }

    private void addMetricProfileItem(List<MetricProfileItem> list, String name, long minValue, long maxValue) {
        list.add(new MetricProfileItem(name, true, minValue, maxValue, 0.0, 0.0));
    }

    private void addMetricProfileItem(List<MetricProfileItem> list, String name, double minValue, double maxValue) {
        list.add(new MetricProfileItem(name, false, 0, 0, minValue, maxValue));
    }

    public Map<String, List<MetricProfileItem>> getProfiles() {
        return new TreeMap<>(profiles);
    }

    public void setProfiles(Map<String, List<MetricProfileItem>> profiles) {
        this.profiles.clear();
        this.profiles.putAll(profiles);
    }

    @Override
    public synchronized MetricProfileSettings1 getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull MetricProfileSettings1 state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}