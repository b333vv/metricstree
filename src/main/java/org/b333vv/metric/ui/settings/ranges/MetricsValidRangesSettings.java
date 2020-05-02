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
        putToControlledMetricsMap(WMC, 0, 24);
        putToControlledMetricsMap(DIT, 0, 5);
        putToControlledMetricsMap(CBO, 0, 13);
        putToControlledMetricsMap(RFC, 0, 44);

        //Lorenz-Kidd metrics set
        putToControlledMetricsMap(NOA, 0, 40);
        putToControlledMetricsMap(NOO, 0, 20);
        putToControlledMetricsMap(NOOM, 0, 3);

        //Robert C. Martin metrics set
        putToControlledMetricsMap(Ce, 0, 20);
        putToControlledMetricsMap(Ca, 0, 500);
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

        //Methods metrics set
        putToControlledMetricsMap(LOC, 0, 500);

        //-----------------------------
        //Chidamber-Kemerer metrics set
        putToUnControlledMetricsMap(NOC, 0, 100);
        putToUnControlledMetricsMap(LCOM, 0, 500);

        //Lorenz-Kidd metrics set
        putToUnControlledMetricsMap(NOAM, 0, 10);

        //Li-Henry metrics set
        putToUnControlledMetricsMap(SIZE2, 0, 130);
        putToUnControlledMetricsMap(NOM, 0, 25);
        putToUnControlledMetricsMap(MPC, 0, 10);
        putToUnControlledMetricsMap(DAC, 0, 15);

        //Methods metrics set
        putToUnControlledMetricsMap(CND, 0, 50);
        putToUnControlledMetricsMap(LND, 0, 4);
        putToUnControlledMetricsMap(CC, 0, 50);
        putToUnControlledMetricsMap(NOL, 0, 10);
    }

    private void putToControlledMetricsMap(MetricType type, long minLongValue, long maxLongValue) {
        putToMetricsMap(controlledMetrics, type, false, 0.0, 0.0, minLongValue, maxLongValue);
    }

    private void putToControlledMetricsMap(MetricType type, double minDoubleValue, double maxDoubleValue) {
        putToMetricsMap(controlledMetrics, type, true, minDoubleValue, maxDoubleValue, 0, 0);
    }

    private void putToUnControlledMetricsMap(MetricType type, long minLongValue, long maxLongValue) {
        putToMetricsMap(unControlledMetrics, type, false, 0.0, 0.0, minLongValue, maxLongValue);
    }

    private void putToUnControlledMetricsMap(MetricType type, double minDoubleValue, double maxDoubleValue) {
        putToMetricsMap(unControlledMetrics, type, true, minDoubleValue, maxDoubleValue, 0, 0);
    }

    private void putToMetricsMap(Map<String, MetricsValidRangeStub> map,
                                 MetricType type, boolean doubleValue,
                                 double minDoubleValue, double maxDoubleValue,
                                 long minLongValue, long maxLongValue) {
        map.put(type.name(), new MetricsValidRangeStub(type.name(), type.description(), type.level().level(),
                doubleValue, minDoubleValue, maxDoubleValue, minLongValue, maxLongValue));
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