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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@State(name = "MetricsValidRanges", storages = {@Storage("metrics-valid-ranges.xml")})
public final class MetricsValidRangesSettings implements PersistentStateComponent<MetricsValidRangesSettings>, ProjectComponent {

    private boolean controlValidRanges;

    private static final String PROJECT_LEVEL = "Project";
    private static final String PACKAGE_LEVEL = "Package";
    private static final String CLASS_LEVEL = "Class";
    private static final String METHOD_LEVEL = "Method";

    private final Map<String, MetricsValidRangeStub> controlledMetrics = new HashMap<>();
    private final Map<String, MetricsValidRangeStub> unControlledMetrics = new HashMap<>();
    private final Map<String, MetricsValidRangeStub> temporaryControlledMetrics = new HashMap<>();
    private final Map<String, MetricsValidRangeStub> temporaryUnControlledMetrics = new HashMap<>();

    public MetricsValidRangesSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        controlValidRanges = true;

        //Chidamber-Kemerer metrics set
        controlledMetrics.put("WMC", new MetricsValidRangeStub("WMC", "Weighted Methods Per Class", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 24));
        controlledMetrics.put("DIT", new MetricsValidRangeStub("DIT", "Depth Of Inheritance Tree", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 5));
        controlledMetrics.put("CBO", new MetricsValidRangeStub("CBO", "Coupling Between Object", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 13));
        controlledMetrics.put("RFC", new MetricsValidRangeStub("RFC", "Response For A Class", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 44));


        //Lorenz-Kidd metrics set
        controlledMetrics.put("NOA", new MetricsValidRangeStub("NOA", "Number Of Attributes", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 40));
        controlledMetrics.put("NOO", new MetricsValidRangeStub("NOO", "Number Of Operations", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 20));
        controlledMetrics.put("NOOM", new MetricsValidRangeStub("NOOM", "Number of Overridden Methods", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 3));

        //Robert C. Martin metrics set
        controlledMetrics.put("Ce", new MetricsValidRangeStub("Ce", "Efferent Coupling", PACKAGE_LEVEL,
                false, 0.00, 0.00, 0, 20));
        controlledMetrics.put("Ca", new MetricsValidRangeStub("Ca", "Afferent Coupling", PACKAGE_LEVEL,
                false, 0.00, 0.00, 0, 500));
        controlledMetrics.put("I", new MetricsValidRangeStub("I", "Instability", PACKAGE_LEVEL,
                true, 0.00, 1.00, 0, 0));
        controlledMetrics.put("A", new MetricsValidRangeStub("A", "Abstractness", PACKAGE_LEVEL,
                true, 0.00, 1.00, 0, 0));
        controlledMetrics.put("D", new MetricsValidRangeStub("D", "Normalized Distance From Main Sequence", PACKAGE_LEVEL,
                true, 0.00, 0.70, 0, 0));

        //MOOD metrics set
        controlledMetrics.put("MHF", new MetricsValidRangeStub("MHF", "Method Hiding Factor", PROJECT_LEVEL,
                true, 0.095, 0.369, 0, 0));
        controlledMetrics.put("AHF", new MetricsValidRangeStub("AHF", "Attribute Hiding Factor", PROJECT_LEVEL,
                true, 0.677, 1.0, 0, 0));
        controlledMetrics.put("MIF", new MetricsValidRangeStub("MIF", "Method Inheritance Factor", PROJECT_LEVEL,
                true, 0.609, 0.844, 0, 0));
        controlledMetrics.put("AIF", new MetricsValidRangeStub("AIF", "Attribute Inheritance Factor", PROJECT_LEVEL,
                true, 0.374, 0.757, 0, 0));
        controlledMetrics.put("CF", new MetricsValidRangeStub("CF", "Coupling Factor", PROJECT_LEVEL,
                true, 0.00, 0.243, 0, 0));
        controlledMetrics.put("PF", new MetricsValidRangeStub("PF", "Polymorphism Factor", PROJECT_LEVEL,
                true, 0.017, 0.151, 0, 0));

        //Methods metrics set
        controlledMetrics.put("LOC", new MetricsValidRangeStub("LOC", "Lines Of Code", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 500));


        //Chidamber-Kemerer metrics set
        unControlledMetrics.put("NOC", new MetricsValidRangeStub("NOC", "Number Of Children", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 100));
        unControlledMetrics.put("LCOM", new MetricsValidRangeStub("LCOM", "Lack Of Cohesion In Methods", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 500));

        //Lorenz-Kidd metrics set
        unControlledMetrics.put("NOAM", new MetricsValidRangeStub("NOAM", "Number Of Added Methods", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 10));

        //Li-Henry metrics set
        unControlledMetrics.put("SIZE2", new MetricsValidRangeStub("SIZE2", "Number Of Attributes And Methods", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 130));
        unControlledMetrics.put("NOM", new MetricsValidRangeStub("NOM", "Number Of Methods", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 25));
        unControlledMetrics.put("MPC", new MetricsValidRangeStub("MPC", "Message Passing Coupling", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 10));
        unControlledMetrics.put("DAC", new MetricsValidRangeStub("DAC", "Data Abstraction Coupling", CLASS_LEVEL,
                false, 0.00, 0.00, 0, 15));

        //Methods metrics set
        unControlledMetrics.put("CND", new MetricsValidRangeStub("CND", "Condition Nesting Depth", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 50));
        unControlledMetrics.put("LND", new MetricsValidRangeStub("LND", "Loop Nesting Depth", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 4));
        unControlledMetrics.put("CC", new MetricsValidRangeStub("CC", "McCabe Cyclomatic Complexity", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 50));
        unControlledMetrics.put("NOL", new MetricsValidRangeStub("NOL", "Number Of Loops", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 10));
        unControlledMetrics.put("FIN", new MetricsValidRangeStub("FIN", "Fan-In", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 5));
        unControlledMetrics.put("FOUT", new MetricsValidRangeStub("FOUT", "Fan-Out", METHOD_LEVEL,
                false, 0.00, 0.00, 0, 5));
    }

    public boolean isControlValidRanges() {
        return controlValidRanges;
    }

    public void setControlValidRanges(boolean controlValidRanges) {
        this.controlValidRanges = controlValidRanges;
    }

    public Map<String, MetricsValidRangeStub> getControlledMetrics() {
        return new HashMap<>(controlledMetrics);
    }

    public void setControlledMetrics(Map<String, MetricsValidRangeStub> metrics) {
        controlledMetrics.clear();
        controlledMetrics.putAll(metrics);
    }

    public Map<String, MetricsValidRangeStub> getUnControlledMetrics() {
        return new HashMap<>(unControlledMetrics);
    }

    public void setUnControlledMetrics(Map<String, MetricsValidRangeStub> metrics) {
        unControlledMetrics.clear();
        unControlledMetrics.putAll(metrics);
    }

    public List<MetricsValidRangeStub> getControlledMetricsList() {
        Comparator<MetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(MetricsValidRangeStub::getLevel)
                .thenComparing(MetricsValidRangeStub::getName);
        return controlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public List<MetricsValidRangeStub> getUnControlledMetricsList() {
        Comparator<MetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(MetricsValidRangeStub::getLevel)
                .thenComparing(MetricsValidRangeStub::getName);
        return unControlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized MetricsValidRangesSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull MetricsValidRangesSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MetricsAllowableValueRangesSettings";
    }

    public void removeFromUnControlledMetrics(String key) {
        temporaryControlledMetrics.put(key, unControlledMetrics.get(key));
        unControlledMetrics.remove(key);
    }

    public void addToUnControlledMetrics(MetricsValidRangeStub metricsValidRangeStub) {
        unControlledMetrics.put(metricsValidRangeStub.getName(), metricsValidRangeStub);
        temporaryUnControlledMetrics.put(metricsValidRangeStub.getName(), metricsValidRangeStub);
    }

    public void returnAllToUnControlledMetrics() {
        unControlledMetrics.putAll(temporaryControlledMetrics);
    }

    public void returnAllToControlledMetrics() {
        unControlledMetrics.entrySet().removeIf(e -> temporaryUnControlledMetrics.containsKey(e.getKey()));
    }

    public void clearTemporaryControlledMetrics() {
        temporaryControlledMetrics.clear();
    }

    public void clearTemporaryUnControlledMetrics() {
        temporaryUnControlledMetrics.clear();
    }
}
