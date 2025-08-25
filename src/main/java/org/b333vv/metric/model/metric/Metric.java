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

package org.b333vv.metric.model.metric;

import org.b333vv.metric.model.metric.value.Value;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.b333vv.metric.model.metric.value.Value.UNDEFINED;

public class Metric implements Comparable<Metric> {
    private final MetricType type;
    private Value psiValue;
    private Value javaParserValue;

    private Metric(MetricType type, Value psiValue) {
        this.type = type;
        this.psiValue = psiValue;
        this.javaParserValue = Value.UNDEFINED;
    }

    public static Metric of(MetricType type, Value psiValue) {
        return new Metric(type, psiValue);
    }

    public static Metric of(MetricType type, long psiValue) {
        return new Metric(type, Value.of(psiValue));
    }

    public static Metric of(MetricType type, double psiValue) {
        return new Metric(type, Value.of(psiValue));
    }

    public MetricType getType() {
        return type;
    }

    @Deprecated
    public Value getValue() {
        return getPsiValue();
    }

    public Value getPsiValue() {
        return psiValue;
    }

    public void setPsiValue(Value psiValue) {
        this.psiValue = psiValue;
    }

    public Value getJavaParserValue() {
        return javaParserValue;
    }

    public void setJavaParserValue(Value javaParserValue) {
        this.javaParserValue = javaParserValue;
    }

    @Override
    public String toString() {
        return type.name() + ": " + psiValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metric)) return false;
        Metric metric = (Metric) o;
        return getType() == metric.getType() &&
                Objects.equals(getPsiValue(), metric.getPsiValue()) &&
                Objects.equals(getJavaParserValue(), metric.getJavaParserValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getPsiValue(), getJavaParserValue());
    }

    public String getFormattedValue() {
        return psiValue.toString();
    }

    @Override
    public int compareTo(@NotNull Metric o) {
        Value that = o.getPsiValue();
        return this.getPsiValue().compareTo(that);
    }
}
