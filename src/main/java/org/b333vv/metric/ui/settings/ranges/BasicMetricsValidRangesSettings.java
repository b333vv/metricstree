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

@State(name = "BasicMetricsValidRanges", storages = {@Storage("basic-metrics-valid-ranges.xml")})
public final class BasicMetricsValidRangesSettings implements PersistentStateComponent<BasicMetricsValidRangesSettings> {

    private boolean controlValidRanges;

    private final Map<String, BasicMetricsValidRangeStub> controlledMetrics = new HashMap<>();
    private final Map<String, BasicMetricsValidRangeStub> unControlledMetrics = new HashMap<>();
    private final Map<String, BasicMetricsValidRangeStub> temporaryControlledMetrics = new HashMap<>();
    private final Map<String, BasicMetricsValidRangeStub> temporaryUnControlledMetrics = new HashMap<>();

    public BasicMetricsValidRangesSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {

        controlValidRanges = true;

        //--CONTROLLED---------------------------------------------------
        //Chidamber-Kemerer metrics set
        putToControlledMetricsMap(WMC, 12, 35, 45);
        putToControlledMetricsMap(DIT, 3, 5, 7);
        putToControlledMetricsMap(CBO, 14, 17, 23);
        putToControlledMetricsMap(RFC, 45, 65, 80);
        putToControlledMetricsMap(NOC, 2, 4, 7);

        //Lorenz-Kidd metrics set
        putToControlledMetricsMap(NOA, 4, 9, 14);
        putToControlledMetricsMap(NOOM, 3, 5, 8);

        //Li-Henry metrics set
        putToControlledMetricsMap(NOM, 7, 15, 25);

        //Robert C. Martin metrics set
        putToControlledMetricsMap(Ce, 7, 17, 33);
        putToControlledMetricsMap(Ca, 8, 40, 65);

        //Methods metrics set
        putToControlledMetricsMap(LOC, 11, 31, 47);
        putToControlledMetricsMap(CC, 3, 5, 7);
        putToControlledMetricsMap(CND, 2, 3, 4);
        putToControlledMetricsMap(LND, 2, 3, 4);
        putToControlledMetricsMap(NOPM, 3, 4, 5);
        putToControlledMetricsMap(FDP, 3, 5, 7);
        putToControlledMetricsMap(NOAV, 3, 5, 7);
        putToControlledMetricsMap(MND, 3, 5, 7);
        putToControlledMetricsMap(CINT, 7, 11, 15);

        //Lanza-Marinescu metrics set
        putToControlledMetricsMap(ATFD, 6, 8, 10);
        putToControlledMetricsMap(NOPA, 3, 5, 12);
        putToControlledMetricsMap(NOAC, 4, 7, 13);

        //Chr. Clemens Lee metrics set
        putToControlledMetricsMap(NCSS, 1000, 1500, 2000);

        //--UNCONTROLLED-------------------------------------------------
        //Chidamber-Kemerer metrics set

        putToUnControlledMetricsMap(LCOM, 51, 75, 90);

        //Lorenz-Kidd metrics set
        putToUnControlledMetricsMap(NOAM, 3, 5, 7);
        putToUnControlledMetricsMap(NOO, 30, 50, 70);

        //Li-Henry metrics set
        putToUnControlledMetricsMap(SIZE2, 131, 161, 181);
        putToUnControlledMetricsMap(MPC, 11, 15, 20);
        putToUnControlledMetricsMap(DAC, 16, 22, 34);

        //Methods metrics set
        putToUnControlledMetricsMap(NOL, 5, 7, 9);
    }

    private void putToControlledMetricsMap(MetricType type, long regularLongValue, long highLongValue, long veryHighLongValue) {
        putToMetricsMap(controlledMetrics, type, regularLongValue, highLongValue, veryHighLongValue);
    }

    private void putToUnControlledMetricsMap(MetricType type, long regularLongValue, long highLongValue, long veryHighLongValue) {
        putToMetricsMap(unControlledMetrics, type, regularLongValue, highLongValue, veryHighLongValue);
    }

    private void putToMetricsMap(Map<String, BasicMetricsValidRangeStub> map,
                                 MetricType type,
                                 long regularLongValue, long highLongValue, long veryHighLongValue) {
        map.put(type.name(), new BasicMetricsValidRangeStub(type.name(), type.description(), type.level().level(),
                regularLongValue, highLongValue, veryHighLongValue));
    }

    public boolean isControlValidRanges() {
        return controlValidRanges;
    }

    public void setControlValidRanges(boolean controlValidRanges) {
        this.controlValidRanges = controlValidRanges;
    }

    public Map<String, BasicMetricsValidRangeStub> getControlledMetrics() {
        return new HashMap<>(controlledMetrics);
    }

    public void setControlledMetrics(Map<String, BasicMetricsValidRangeStub> metrics) {
        controlledMetrics.clear();
        controlledMetrics.putAll(metrics);
    }

    public Map<String, BasicMetricsValidRangeStub> getUnControlledMetrics() {
        return new HashMap<>(unControlledMetrics);
    }

    public void setUnControlledMetrics(Map<String, BasicMetricsValidRangeStub> metrics) {
        unControlledMetrics.clear();
        unControlledMetrics.putAll(metrics);
    }

    public List<BasicMetricsValidRangeStub> getControlledMetricsList() {
        Comparator<BasicMetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(BasicMetricsValidRangeStub::getLevel)
                .thenComparing(BasicMetricsValidRangeStub::getName);
        return controlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public List<BasicMetricsValidRangeStub> getUnControlledMetricsList() {
        Comparator<BasicMetricsValidRangeStub> compareByLevelAndName = Comparator
                .comparing(BasicMetricsValidRangeStub::getLevel)
                .thenComparing(BasicMetricsValidRangeStub::getName);
        return unControlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized BasicMetricsValidRangesSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull BasicMetricsValidRangesSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void removeFromUnControlledMetrics(String key) {
        temporaryControlledMetrics.put(key, unControlledMetrics.get(key));
        unControlledMetrics.remove(key);
    }

    public void addToUnControlledMetrics(BasicMetricsValidRangeStub basicMetricsValidRangeStub) {
        unControlledMetrics.put(basicMetricsValidRangeStub.getName(), basicMetricsValidRangeStub);
        temporaryUnControlledMetrics.put(basicMetricsValidRangeStub.getName(), basicMetricsValidRangeStub);
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
    public BasicMetricsValidRangeStub getMetricsAllowableValueRangeStub(MetricType type) {
        return controlledMetrics.get(type.name());
    }
}