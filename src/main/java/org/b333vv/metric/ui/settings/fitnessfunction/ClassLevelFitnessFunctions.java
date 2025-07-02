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

package org.b333vv.metric.ui.settings.fitnessfunction;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Long.MAX_VALUE;
import static org.b333vv.metric.model.metric.MetricType.*;

@State(
        name = "ClassLevelFitnessFunctions",
        storages = {@Storage("class-level-fitness-functions.xml")}
)
public final class ClassLevelFitnessFunctions implements PersistentStateComponent<ClassLevelFitnessFunctions> {

    private final Map<String, List<FitnessFunctionItem>> profiles = new TreeMap<>();
    private final Map<String, String> profilesDescription = new HashMap<>();

    public ClassLevelFitnessFunctions() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        // God Class (type 1)
        List<FitnessFunctionItem> godClass1 = new ArrayList<>();
        addItem(godClass1, WMC.name(), 47, MAX_VALUE);
        addItem(godClass1, ATFD.name(), 6, MAX_VALUE);
        addItem(godClass1, TCC.name(), 0.00, 0.33);
        profiles.put("God Class (type 1)", godClass1);
        profilesDescription.put("God Class (type 1)", "Is a class that single-handedly implements large blocks of functionality within a system and performs multiple, non-cohesive functionalities");

        // God Class (type 2)
        List<FitnessFunctionItem> godClass2 = new ArrayList<>();
        addItem(godClass2, WMC.name(), 44, MAX_VALUE);
        addItem(godClass2, ATFD.name(), 0, 4);
        addItem(godClass2, NOA.name(), 30, MAX_VALUE);
        profiles.put("God Class (type 2)", godClass2);
        profilesDescription.put("God Class (type 2)", "Is a class that single-handedly implements large blocks of functionality within a system and performs multiple, non-cohesive functionalities");

        // God Class (type 3)
        List<FitnessFunctionItem> godClass3 = new ArrayList<>();
        addItem(godClass3, WMC.name(), 44, MAX_VALUE);
        addItem(godClass3, ATFD.name(), 4, MAX_VALUE);
        addItem(godClass3, CBO.name(), 11, MAX_VALUE);
        profiles.put("God Class (type 3)", godClass3);
        profilesDescription.put("God Class (type 3)", "Is a class that single-handedly implements large blocks of functionality within a system and performs multiple, non-cohesive functionalities");

        // God Class (type 4)
        List<FitnessFunctionItem> godClass4 = new ArrayList<>();
        addItem(godClass4, WMC.name(), 46, MAX_VALUE);
        addItem(godClass4, TCC.name(), 0.00, 0.37);
        addItem(godClass4, CBO.name(), 1, MAX_VALUE);
        addItem(godClass4, RFC.name(), 144, MAX_VALUE);
        profiles.put("God Class (type 4)", godClass4);
        profilesDescription.put("God Class (type 4)", "Is a class that single-handedly implements large blocks of functionality within a system and performs multiple, non-cohesive functionalities");

        // High Coupling
        List<FitnessFunctionItem> highCoupling = new ArrayList<>();
        addItem(highCoupling, CBO.name(), 20, MAX_VALUE);
        profiles.put("High Coupling", highCoupling);
        profilesDescription.put("High Coupling", "A class whose change affects many others, making the system complex, rigid, and fragile");

        // Long Parameters List
        List<FitnessFunctionItem> longParametersList = new ArrayList<>();
        addItem(longParametersList, NOPM.name(), 4, MAX_VALUE);
        profiles.put("Long Parameters List", longParametersList);
        profilesDescription.put("Long Parameters List", "Might happen after several types of algorithms are merged in a single method");

        // Long Method
        List<FitnessFunctionItem> longMethods = new ArrayList<>();
        addItem(longMethods, LOC.name(), 16, MAX_VALUE);
        profiles.put("Long Method", longMethods);
        profilesDescription.put("Long Method", "The longer the method, the more difficult it is to understand, modify, or extend. It is a sign that the method is doing too much");

        // Complex Method
        List<FitnessFunctionItem> complexMethods = new ArrayList<>();
        addItem(complexMethods, CC.name(), 8, MAX_VALUE);
        profiles.put("Complex Method", complexMethods);
        profilesDescription.put("Complex Method", "Potential difficulties in understanding and maintaining the code");

        // Feature Envy
        List<FitnessFunctionItem> featureEnvy = new ArrayList<>();
        addItem(featureEnvy, ATFD.name(), 5, MAX_VALUE);
        addItem(featureEnvy, FDP.name(), 5, MAX_VALUE);
        addItem(featureEnvy, LAA.name(), 0.00, 0.33);
        profiles.put("Feature Envy", featureEnvy);
        profilesDescription.put("Feature Envy", "Indicates that the method is in the wrong place and is more tightly coupled to the other class than to the one where it is currently located");

        // Brain Method
        List<FitnessFunctionItem> brainMethod = new ArrayList<>();
        addItem(brainMethod, LOC.name(), 30, MAX_VALUE);
        addItem(brainMethod, CC.name(), 3, MAX_VALUE);
        addItem(brainMethod, MND.name(), 3, MAX_VALUE);
        addItem(brainMethod, NOAV.name(), 3, MAX_VALUE);
        profiles.put("Brain Method", brainMethod);
        profilesDescription.put("Brain Method", "Is a method that seeks to centralize the functionality of the owner class, performs most of its tasks that should be distributed among several methods");

        // Brain Class
        List<FitnessFunctionItem> brainClass = new ArrayList<>();
        addItem(brainClass, LOC.name(), 30, MAX_VALUE);
        addItem(brainClass, CC.name(), 3, MAX_VALUE);
        addItem(brainClass, MND.name(), 3, MAX_VALUE);
        addItem(brainClass, NOAV.name(), 3, MAX_VALUE);
        addItem(brainClass, WMC.name(), 34, MAX_VALUE);
        addItem(brainClass, TCC.name(), 0.00, 0.50);
        profiles.put("Brain Class", brainClass);
        profilesDescription.put("Brain Class", "This is a complex class that implements too much system intelligence, making it difficult to understand and maintain");

        // Intensive Coupling
        List<FitnessFunctionItem> intensiveCoupling1 = new ArrayList<>();
        addItem(intensiveCoupling1, CINT.name(), 8, MAX_VALUE);
        addItem(intensiveCoupling1, CDISP.name(), 0.00, 0.50);
        addItem(intensiveCoupling1, MND.name(), 2, MAX_VALUE);
        profiles.put("Intensive Coupling", intensiveCoupling1);
        profilesDescription.put("Intensive Coupling", "Understanding the relation between the client method and the classes providing services becomes difficult");

        // Dispersed Coupling
        List<FitnessFunctionItem> dispersedCoupling = new ArrayList<>();
        addItem(dispersedCoupling, CINT.name(), 8, MAX_VALUE);
        addItem(dispersedCoupling, CDISP.name(), 0.66, MAX_VALUE);
        addItem(dispersedCoupling, MND.name(), 2, MAX_VALUE);
        profiles.put("Dispersed Coupling", dispersedCoupling);
        profilesDescription.put("Dispersed Coupling", "A change in a dispersively coupled method leads to changes in all the dependent (coupled) classes");

        // Deeply Nested Conditions
        List<FitnessFunctionItem> deeplyNestedConditions = new ArrayList<>();
        addItem(deeplyNestedConditions, CND.name(), 3, MAX_VALUE);
        profiles.put("Deeply Nested Conditions", deeplyNestedConditions);
        profilesDescription.put("Deeply Nested Conditions", "The more nested conditions, the more difficult it is to understand and maintain the code");

        // Too Many Fields
        List<FitnessFunctionItem> tooManyFields = new ArrayList<>();
        addItem(tooManyFields, NOA.name(), 15, MAX_VALUE);
        profiles.put("Too Many Fields", tooManyFields);
        profilesDescription.put("Too Many Fields", "A class with too many fields is difficult to understand and maintain");

        // Too Many Methods
        List<FitnessFunctionItem> tooManyMethods = new ArrayList<>();
        addItem(tooManyMethods, NOM.name(), 10, MAX_VALUE);
        profiles.put("Too Many Methods", tooManyMethods);
        profilesDescription.put("Too Many Methods", "A class with too many methods is difficult to understand and maintain");

        // Data Class
        List<FitnessFunctionItem> dataClass = new ArrayList<>();
        addItem(dataClass, WMC.name(), 0, 15);
        addItem(dataClass, WOC.name(), 0.00, 0.34);
        addItem(dataClass, NOAM.name(), 4, MAX_VALUE);
        addItem(dataClass, NOPA.name(), 3, MAX_VALUE);
        profiles.put("Data Class", dataClass);
        profilesDescription.put("Data Class", "A class does not implement enough functionality itself to justify it being a class");

    }

    private void addItem(List<FitnessFunctionItem> list, String name, long minValue, long maxValue) {
        list.add(new FitnessFunctionItem(name, true, minValue, maxValue, 0.0, 0.0));
    }

    private void addItem(List<FitnessFunctionItem> list, String name, double minValue, double maxValue) {
        list.add(new FitnessFunctionItem(name, false, 0, 0, minValue, maxValue));
    }

    public Map<String, List<FitnessFunctionItem>> getProfiles() {
        return new TreeMap<>(profiles);
    }

    public Map<String, String> getProfilesDescription() {
        return new HashMap<>(profilesDescription);
    }

    public void setProfiles(Map<String, List<FitnessFunctionItem>> profiles) {
        this.profiles.clear();
        this.profiles.putAll(profiles);
    }

    public void setProfilesDescription(Map<String, String> profilesDescription) {
        this.profilesDescription.clear();
        this.profilesDescription.putAll(profilesDescription);
    }

    @Override
    public synchronized ClassLevelFitnessFunctions getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull ClassLevelFitnessFunctions state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}