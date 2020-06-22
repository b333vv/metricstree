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

@State(name = "DerivativeMetricsValidRanges", storages = {@Storage("derivative-metrics-valid-ranges.xml")})
public final class DerivativeMetricsValidRangesSettings implements PersistentStateComponent<DerivativeMetricsValidRangesSettings> {

    private boolean controlValidRanges;

    private final Map<String, DerivativeMetricsValidRangeStub> controlledMetrics = new HashMap<>();
    private final Map<String, DerivativeMetricsValidRangeStub> unControlledMetrics = new HashMap<>();
    private final Map<String, DerivativeMetricsValidRangeStub> temporaryControlledMetrics = new HashMap<>();
    private final Map<String, DerivativeMetricsValidRangeStub> temporaryUnControlledMetrics = new HashMap<>();

    public DerivativeMetricsValidRangesSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        controlValidRanges = true;

        //--CONTROLLED---------------------------------------------------

        //Method level
        putToControlledMetricsMap(LAA, 0.33, 1.00);
        putToControlledMetricsMap(CDISP, 0.00, 0.50);

        //Bieman-Kang metrics set
        putToControlledMetricsMap(TCC, 0.33, 1.00);

        //Lanza-Marinescu metrics set
        putToControlledMetricsMap(WOC, 0.50, 1.00);

        //Robert C. Martin metrics set
        putToControlledMetricsMap(I, 0.00, 1.00);
        putToControlledMetricsMap(A, 0.00, 1.00);
        putToControlledMetricsMap(D, 0.00, 0.70);

        //MOOD metrics set
        putToControlledMetricsMap(MHF, 0.095, 0.369);
        putToControlledMetricsMap(AHF, 0.677, 1.00);
        putToControlledMetricsMap(MIF, 0.609, 0.844);
        putToControlledMetricsMap(AIF, 0.374, 0.757);
        putToControlledMetricsMap(CF, 0.00, 0.243);
        putToControlledMetricsMap(PF, 0.017, 0.151);
    }

    private void putToControlledMetricsMap(MetricType type, double from, double to) {
        putToMetricsMap(controlledMetrics, type, from, to);
    }

    private void putToUnControlledMetricsMap(MetricType type, double from, double to) {
        putToMetricsMap(unControlledMetrics, type, from, to);
    }

    private void putToMetricsMap(Map<String, DerivativeMetricsValidRangeStub> map,
                                 MetricType type, double fromDoubleValue, double toDoubleValue) {
        map.put(type.name(), new DerivativeMetricsValidRangeStub(type.name(), type.description(), type.level().level(),
                fromDoubleValue, toDoubleValue));
    }

    public boolean isControlValidRanges() {
        return controlValidRanges;
    }

    public void setControlValidRanges(boolean controlValidRanges) {
        this.controlValidRanges = controlValidRanges;
    }

    public Map<String, DerivativeMetricsValidRangeStub> getControlledMetrics() {
        return new HashMap<>(controlledMetrics);
    }

    public void setControlledMetrics(Map<String, DerivativeMetricsValidRangeStub> metrics) {
        controlledMetrics.clear();
        controlledMetrics.putAll(metrics);
    }

    public Map<String, DerivativeMetricsValidRangeStub> getUnControlledMetrics() {
        return new HashMap<>(unControlledMetrics);
    }

    public void setUnControlledMetrics(Map<String, DerivativeMetricsValidRangeStub> metrics) {
        unControlledMetrics.clear();
        unControlledMetrics.putAll(metrics);
    }

    public List<DerivativeMetricsValidRangeStub> getControlledMetricsList() {
        Comparator<DerivativeMetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(DerivativeMetricsValidRangeStub::getLevel)
                .thenComparing(DerivativeMetricsValidRangeStub::getName);
        return controlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public List<DerivativeMetricsValidRangeStub> getUnControlledMetricsList() {
        Comparator<DerivativeMetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(DerivativeMetricsValidRangeStub::getLevel)
                .thenComparing(DerivativeMetricsValidRangeStub::getName);
        return unControlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized DerivativeMetricsValidRangesSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull DerivativeMetricsValidRangesSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void removeFromUnControlledMetrics(String key) {
        temporaryControlledMetrics.put(key, unControlledMetrics.get(key));
        unControlledMetrics.remove(key);
    }

    public void addToUnControlledMetrics(DerivativeMetricsValidRangeStub derivativeMetricsValidRangeStub) {
        unControlledMetrics.put(derivativeMetricsValidRangeStub.getName(), derivativeMetricsValidRangeStub);
        temporaryUnControlledMetrics.put(derivativeMetricsValidRangeStub.getName(), derivativeMetricsValidRangeStub);
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
    public DerivativeMetricsValidRangeStub getMetricsAllowableValueRangeStub(MetricType type) {
        return controlledMetrics.get(type.name());
    }
}