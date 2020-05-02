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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

@State(name = "MetricsValidRanges", storages = {@Storage("metrics-valid-ranges.xml")})
public final class MetricsValidRangesSettings implements PersistentStateComponent<MetricsValidRangesSettings>, BaseComponent {

    private boolean controlValidRanges;

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
        controlledMetrics.put(WMC.name(), new MetricsValidRangeStub(WMC.name(), WMC.description(), WMC.level().level(),
                false, 0.00, 0.00, 0, 24));
        controlledMetrics.put(DIT.name(), new MetricsValidRangeStub(DIT.name(), DIT.description(), DIT.level().level(),
                false, 0.00, 0.00, 0, 5));
        controlledMetrics.put(CBO.name(), new MetricsValidRangeStub(CBO.name(), CBO.description(), CBO.level().level(),
                false, 0.00, 0.00, 0, 13));
        controlledMetrics.put(RFC.name(), new MetricsValidRangeStub(RFC.name(), RFC.description(), RFC.level().level(),
                false, 0.00, 0.00, 0, 44));


        //Lorenz-Kidd metrics set
        controlledMetrics.put(NOA.name(), new MetricsValidRangeStub(NOA.name(), NOA.description(), NOA.level().level(),
                false, 0.00, 0.00, 0, 40));
        controlledMetrics.put(NOO.name(), new MetricsValidRangeStub(NOO.name(), NOO.description(), NOO.level().level(),
                false, 0.00, 0.00, 0, 20));
        controlledMetrics.put(NOOM.name(), new MetricsValidRangeStub(NOOM.name(), NOOM.description(), NOOM.level().level(),
                false, 0.00, 0.00, 0, 3));

        //Robert C. Martin metrics set
        controlledMetrics.put(Ce.name(), new MetricsValidRangeStub(Ce.name(), Ce.description(), Ce.level().level(),
                false, 0.00, 0.00, 0, 20));
        controlledMetrics.put(Ca.name(), new MetricsValidRangeStub(Ca.name(), Ca.description(), Ca.level().level(),
                false, 0.00, 0.00, 0, 500));
        controlledMetrics.put(I.name(), new MetricsValidRangeStub(I.name(), I.description(), I.level().level(),
                true, 0.00, 1.00, 0, 0));
        controlledMetrics.put(A.name(), new MetricsValidRangeStub(A.name(), A.description(), A.level().level(),
                true, 0.00, 1.00, 0, 0));
        controlledMetrics.put(D.name(), new MetricsValidRangeStub(D.name(), D.description(), D.level().level(),
                true, 0.00, 0.70, 0, 0));

        //MOOD metrics set
        controlledMetrics.put(MHF.name(), new MetricsValidRangeStub(MHF.name(), MHF.description(), MHF.level().level(),
                true, 0.095, 0.369, 0, 0));
        controlledMetrics.put(AHF.name(), new MetricsValidRangeStub(AHF.name(), AHF.description(), AHF.level().level(),
                true, 0.677, 1.0, 0, 0));
        controlledMetrics.put(MIF.name(), new MetricsValidRangeStub(MIF.name(), MIF.description(), MIF.level().level(),
                true, 0.609, 0.844, 0, 0));
        controlledMetrics.put(AIF.name(), new MetricsValidRangeStub(AIF.name(), AIF.description(), AIF.level().level(),
                true, 0.374, 0.757, 0, 0));
        controlledMetrics.put(CF.name(), new MetricsValidRangeStub(CF.name(), CF.description(), CF.level().level(),
                true, 0.00, 0.243, 0, 0));
        controlledMetrics.put(PF.name(), new MetricsValidRangeStub(PF.name(), PF.description(), PF.level().level(),
                true, 0.017, 0.151, 0, 0));

        //Methods metrics set
        controlledMetrics.put(LOC.name(), new MetricsValidRangeStub(LOC.name(), LOC.description(), LOC.level().level(),
                false, 0.00, 0.00, 0, 500));


        //Chidamber-Kemerer metrics set
        unControlledMetrics.put(NOC.name(), new MetricsValidRangeStub(NOC.name(), NOC.description(), NOC.level().level(),
                false, 0.00, 0.00, 0, 100));
        unControlledMetrics.put(LCOM.name(), new MetricsValidRangeStub(LCOM.name(), LCOM.description(), LCOM.level().level(),
                false, 0.00, 0.00, 0, 500));

        //Lorenz-Kidd metrics set
        unControlledMetrics.put(NOAM.name(), new MetricsValidRangeStub(NOAM.name(), NOAM.description(), NOAM.level().level(),
                false, 0.00, 0.00, 0, 10));

        //Li-Henry metrics set
        unControlledMetrics.put(SIZE2.name(), new MetricsValidRangeStub(SIZE2.name(), SIZE2.description(), SIZE2.level().level(),
                false, 0.00, 0.00, 0, 130));
        unControlledMetrics.put(NOM.name(), new MetricsValidRangeStub(NOM.name(), NOM.description(), NOM.level().level(),
                false, 0.00, 0.00, 0, 25));
        unControlledMetrics.put(MPC.name(), new MetricsValidRangeStub(MPC.name(), MPC.description(), MPC.level().level(),
                false, 0.00, 0.00, 0, 10));
        unControlledMetrics.put(DAC.name(), new MetricsValidRangeStub(DAC.name(), DAC.description(), DAC.level().level(),
                false, 0.00, 0.00, 0, 15));

        //Methods metrics set
        unControlledMetrics.put(CND.name(), new MetricsValidRangeStub(CND.name(), CND.description(), CND.level().level(),
                false, 0.00, 0.00, 0, 50));
        unControlledMetrics.put(LND.name(), new MetricsValidRangeStub(LND.name(), LND.description(), LND.level().level(),
                false, 0.00, 0.00, 0, 4));
        unControlledMetrics.put(CC.name(), new MetricsValidRangeStub(CC.name(), CC.description(), CC.level().level(),
                false, 0.00, 0.00, 0, 50));
        unControlledMetrics.put(NOL.name(), new MetricsValidRangeStub(NOL.name(), NOL.description(), NOL.level().level(),
                false, 0.00, 0.00, 0, 10));
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

    @Nullable
    public MetricsValidRangeStub getMetricsAllowableValueRangeStub(MetricType type) {
        return controlledMetrics.get(type.name());
    }
}