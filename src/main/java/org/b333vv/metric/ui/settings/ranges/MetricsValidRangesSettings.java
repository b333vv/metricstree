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

package org.b333vv.metric.ui.settings.ranges;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.b333vv.metric.model.metric.MetricType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

@State(name = "MetricsValidRanges", storages = {@Storage("metrics-valid-ranges.xml")})
public final class MetricsValidRangesSettings implements PersistentStateComponent<MetricsValidRangesSettings>, BaseComponent {

    private boolean controlValidRanges;

    private final Map<MetricType, MetricsValidRangeStub> controlledMetrics = new EnumMap<>(MetricType.class);
    private final Map<MetricType, MetricsValidRangeStub> unControlledMetrics = new EnumMap<>(MetricType.class);
    private final Map<MetricType, MetricsValidRangeStub> temporaryControlledMetrics = new EnumMap<>(MetricType.class);
    private final Map<MetricType, MetricsValidRangeStub> temporaryUnControlledMetrics = new EnumMap<>(MetricType.class);
//    private final Map<String, MetricsValidRangeStub> controlledMetrics = new HashMap<>();
//    private final Map<String, MetricsValidRangeStub> unControlledMetrics = new HashMap<>();
//    private final Map<String, MetricsValidRangeStub> temporaryControlledMetrics = new HashMap<>();
//    private final Map<String, MetricsValidRangeStub> temporaryUnControlledMetrics = new HashMap<>();

    public MetricsValidRangesSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        controlValidRanges = true;

//        //Chidamber-Kemerer metrics set
//        controlledMetrics.put(WMC.name(), new MetricsValidRangeStub(WMC, 0, 24));
//        controlledMetrics.put(DIT.name(), new MetricsValidRangeStub(DIT, 0, 5));
//        controlledMetrics.put(CBO.name(), new MetricsValidRangeStub(CBO, 0, 13));
//        controlledMetrics.put(RFC.name(), new MetricsValidRangeStub(RFC, 0, 44));
//
//        //Lorenz-Kidd metrics set
//        controlledMetrics.put(NOA.name(), new MetricsValidRangeStub(NOA, 0, 40));
//        controlledMetrics.put(NOO.name(), new MetricsValidRangeStub(NOO, 0, 20));
//        controlledMetrics.put(NOOM.name(), new MetricsValidRangeStub(NOOM, 0, 3));
//
//        //Robert C. Martin metrics set
//        controlledMetrics.put(Ce.name(), new MetricsValidRangeStub(Ce, 0, 20));
//        controlledMetrics.put(Ca.name(), new MetricsValidRangeStub(Ca, 0, 500));
//        controlledMetrics.put(I.name(), new MetricsValidRangeStub(I, 0.00, 1.00));
//        controlledMetrics.put(A.name(), new MetricsValidRangeStub(A, 0.00, 1.00));
//        controlledMetrics.put(D.name(), new MetricsValidRangeStub(D, 0.00, 0.70));
//
//        //MOOD metrics set
//        controlledMetrics.put(MHF.name(), new MetricsValidRangeStub(MHF, 0.095, 0.369));
//        controlledMetrics.put(AHF.name(), new MetricsValidRangeStub(AHF, 0.677, 1.0));
//        controlledMetrics.put(MIF.name(), new MetricsValidRangeStub(MIF, 0.609, 0.844));
//        controlledMetrics.put(AIF.name(), new MetricsValidRangeStub(AIF, 0.374, 0.757));
//        controlledMetrics.put(CF.name(), new MetricsValidRangeStub(CF, 0.00, 0.243));
//        controlledMetrics.put(PF.name(), new MetricsValidRangeStub(PF, 0.017, 0.151));
//
//        //Methods metrics set
//        controlledMetrics.put(LOC.name(), new MetricsValidRangeStub(LOC, 0, 500));
//
//
//        //Chidamber-Kemerer metrics set
//        unControlledMetrics.put(NOC.name(), new MetricsValidRangeStub(NOC, 0, 100));
//        unControlledMetrics.put(LCOM.name(), new MetricsValidRangeStub(LCOM, 0, 500));
//
//        //Lorenz-Kidd metrics set
//        unControlledMetrics.put(NOAM.name(), new MetricsValidRangeStub(NOAM, 0, 10));
//
//        //Li-Henry metrics set
//        unControlledMetrics.put(SIZE2.name(), new MetricsValidRangeStub(SIZE2, 0, 130));
//        unControlledMetrics.put(NOM.name(), new MetricsValidRangeStub(NOM, 0, 25));
//        unControlledMetrics.put(MPC.name(), new MetricsValidRangeStub(MPC, 0, 10));
//        unControlledMetrics.put(DAC.name(), new MetricsValidRangeStub(DAC, 0, 15));
//
//        //Methods metrics set
//        unControlledMetrics.put(CND.name(), new MetricsValidRangeStub(CND, 0, 50));
//        unControlledMetrics.put(LND.name(), new MetricsValidRangeStub(LND, 0, 4));
//        unControlledMetrics.put(CC.name(), new MetricsValidRangeStub(CC, 0, 50));
//        unControlledMetrics.put(NOL.name(), new MetricsValidRangeStub(NOL, 0, 10));

        //Chidamber-Kemerer metrics set
        controlledMetrics.put(WMC, new MetricsValidRangeStub(WMC, 0, 24));
        controlledMetrics.put(DIT, new MetricsValidRangeStub(DIT, 0, 5));
        controlledMetrics.put(CBO, new MetricsValidRangeStub(CBO, 0, 13));
        controlledMetrics.put(RFC, new MetricsValidRangeStub(RFC, 0, 44));

        //Lorenz-Kidd metrics set
        controlledMetrics.put(NOA, new MetricsValidRangeStub(NOA, 0, 40));
        controlledMetrics.put(NOO, new MetricsValidRangeStub(NOO, 0, 20));
        controlledMetrics.put(NOOM, new MetricsValidRangeStub(NOOM, 0, 3));

        //Robert C. Martin metrics set
        controlledMetrics.put(Ce, new MetricsValidRangeStub(Ce, 0, 20));
        controlledMetrics.put(Ca, new MetricsValidRangeStub(Ca, 0, 500));
        controlledMetrics.put(I, new MetricsValidRangeStub(I, 0.00, 1.00));
        controlledMetrics.put(A, new MetricsValidRangeStub(A, 0.00, 1.00));
        controlledMetrics.put(D, new MetricsValidRangeStub(D, 0.00, 0.70));

        //MOOD metrics set
        controlledMetrics.put(MHF, new MetricsValidRangeStub(MHF, 0.095, 0.369));
        controlledMetrics.put(AHF, new MetricsValidRangeStub(AHF, 0.677, 1.0));
        controlledMetrics.put(MIF, new MetricsValidRangeStub(MIF, 0.609, 0.844));
        controlledMetrics.put(AIF, new MetricsValidRangeStub(AIF, 0.374, 0.757));
        controlledMetrics.put(CF, new MetricsValidRangeStub(CF, 0.00, 0.243));
        controlledMetrics.put(PF, new MetricsValidRangeStub(PF, 0.017, 0.151));

        //Methods metrics set
        controlledMetrics.put(LOC, new MetricsValidRangeStub(LOC, 0, 500));


        //Chidamber-Kemerer metrics set
        unControlledMetrics.put(NOC, new MetricsValidRangeStub(NOC, 0, 100));
        unControlledMetrics.put(LCOM, new MetricsValidRangeStub(LCOM, 0, 500));

        //Lorenz-Kidd metrics set
        unControlledMetrics.put(NOAM, new MetricsValidRangeStub(NOAM, 0, 10));

        //Li-Henry metrics set
        unControlledMetrics.put(SIZE2, new MetricsValidRangeStub(SIZE2, 0, 130));
        unControlledMetrics.put(NOM, new MetricsValidRangeStub(NOM, 0, 25));
        unControlledMetrics.put(MPC, new MetricsValidRangeStub(MPC, 0, 10));
        unControlledMetrics.put(DAC, new MetricsValidRangeStub(DAC, 0, 15));

        //Methods metrics set
        unControlledMetrics.put(CND, new MetricsValidRangeStub(CND, 0, 50));
        unControlledMetrics.put(LND, new MetricsValidRangeStub(LND, 0, 4));
        unControlledMetrics.put(CC, new MetricsValidRangeStub(CC, 0, 50));
        unControlledMetrics.put(NOL, new MetricsValidRangeStub(NOL, 0, 10));

//        //Chidamber-Kemerer metrics set
//        controlledMetrics.put(WMC, new MetricsValidRangeStub(WMC, false, 0.0, 0.0, 0, 24));
//        controlledMetrics.put(DIT, new MetricsValidRangeStub(DIT, false, 0.0, 0.0,0, 5));
//        controlledMetrics.put(CBO, new MetricsValidRangeStub(CBO, false, 0.0, 0.0,0, 13));
//        controlledMetrics.put(RFC, new MetricsValidRangeStub(RFC, false, 0.0, 0.0,0, 44));
//
//        //Lorenz-Kidd metrics set
//        controlledMetrics.put(NOA, new MetricsValidRangeStub(NOA, false, 0.0, 0.0,0, 40));
//        controlledMetrics.put(NOO, new MetricsValidRangeStub(NOO, false, 0.0, 0.0,0, 20));
//        controlledMetrics.put(NOOM, new MetricsValidRangeStub(NOOM, false, 0.0, 0.0,0, 3));
//
//        //Robert C. Martin metrics set
//        controlledMetrics.put(Ce, new MetricsValidRangeStub(Ce, false, 0.0, 0.0,0, 20));
//        controlledMetrics.put(Ca, new MetricsValidRangeStub(Ca, false, 0.0, 0.0,0, 500));
//        controlledMetrics.put(I, new MetricsValidRangeStub(I,  true, 0.00, 1.00, 0, 0));
//        controlledMetrics.put(A, new MetricsValidRangeStub(A, true, 0.00, 1.00, 0, 0));
//        controlledMetrics.put(D, new MetricsValidRangeStub(D, true, 0.00, 0.70, 0, 0));
//
//        //MOOD metrics set
//        controlledMetrics.put(MHF, new MetricsValidRangeStub(MHF, true, 0.095, 0.369, 0, 0));
//        controlledMetrics.put(AHF, new MetricsValidRangeStub(AHF, true, 0.677, 1.0, 0, 0));
//        controlledMetrics.put(MIF, new MetricsValidRangeStub(MIF, true, 0.609, 0.844, 0, 0));
//        controlledMetrics.put(AIF, new MetricsValidRangeStub(AIF, true, 0.374, 0.757, 0, 0));
//        controlledMetrics.put(CF, new MetricsValidRangeStub(CF, true, 0.00, 0.243, 0, 0));
//        controlledMetrics.put(PF, new MetricsValidRangeStub(PF, true, 0.017, 0.151, 0, 0));
//
//        //Methods metrics set
//        controlledMetrics.put(LOC, new MetricsValidRangeStub(LOC, false, 0.0, 0.0,0, 500));
//
//
//        //Chidamber-Kemerer metrics set
//        unControlledMetrics.put(NOC, new MetricsValidRangeStub(NOC, false, 0.0, 0.0,0, 100));
//        unControlledMetrics.put(LCOM, new MetricsValidRangeStub(LCOM, false, 0.0, 0.0,0, 500));
//
//        //Lorenz-Kidd metrics set
//        unControlledMetrics.put(NOAM, new MetricsValidRangeStub(NOAM, false, 0.0, 0.0,0, 10));
//
//        //Li-Henry metrics set
//        unControlledMetrics.put(SIZE2, new MetricsValidRangeStub(SIZE2, false, 0.0, 0.0,0, 130));
//        unControlledMetrics.put(NOM, new MetricsValidRangeStub(NOM, false, 0.0, 0.0,0, 25));
//        unControlledMetrics.put(MPC, new MetricsValidRangeStub(MPC, false, 0.0, 0.0,0, 10));
//        unControlledMetrics.put(DAC, new MetricsValidRangeStub(DAC, false, 0.0, 0.0,0, 15));
//
//        //Methods metrics set
//        unControlledMetrics.put(CND, new MetricsValidRangeStub(CND, false, 0.0, 0.0,0, 50));
//        unControlledMetrics.put(LND, new MetricsValidRangeStub(LND, false, 0.0, 0.0,0, 4));
//        unControlledMetrics.put(CC, new MetricsValidRangeStub(CC, false, 0.0, 0.0,0, 50));
//        unControlledMetrics.put(NOL, new MetricsValidRangeStub(NOL, false, 0.0, 0.0,0, 10));
    }

    public boolean isControlValidRanges() {
        return controlValidRanges;
    }

    public void setControlValidRanges(boolean controlValidRanges) {
        this.controlValidRanges = controlValidRanges;
    }

    public Map<MetricType, MetricsValidRangeStub> getControlledMetrics() {
        return new EnumMap<>(MetricType.class);
    }

    public void setControlledMetrics(Map<MetricType, MetricsValidRangeStub> metrics) {
        controlledMetrics.clear();
        controlledMetrics.putAll(metrics);
    }

    public Map<MetricType, MetricsValidRangeStub> getUnControlledMetrics() {
        return new EnumMap<>(MetricType.class);
    }

    public void setUnControlledMetrics(Map<MetricType, MetricsValidRangeStub> metrics) {
        unControlledMetrics.clear();
        unControlledMetrics.putAll(metrics);
    }

    public List<MetricsValidRangeStub> getControlledMetricsList() {
        Comparator<MetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(MetricsValidRangeStub::getType);
        return controlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public List<MetricsValidRangeStub> getUnControlledMetricsList() {
        Comparator<MetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(MetricsValidRangeStub::getType);
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

    public void removeFromUnControlledMetrics(MetricType key) {
        temporaryControlledMetrics.put(key, unControlledMetrics.get(key));
        unControlledMetrics.remove(key);
    }

    public void addToUnControlledMetrics(MetricsValidRangeStub metricsValidRangeStub) {
        unControlledMetrics.put(metricsValidRangeStub.getType(), metricsValidRangeStub);
        temporaryUnControlledMetrics.put(metricsValidRangeStub.getType(), metricsValidRangeStub);
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

    @Nullable
    public MetricsValidRangeStub getMetricsAllowableValueRangeStub(MetricType type) {
        return controlledMetrics.get(type);
    }
}
