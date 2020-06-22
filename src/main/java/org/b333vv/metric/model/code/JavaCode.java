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

package org.b333vv.metric.model.code;

import com.google.common.base.Objects;
import com.intellij.psi.PsiElementVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class JavaCode {
    private final String name;
    private final Map<MetricType, Metric> metrics;
    protected final Set<JavaCode> children;

    public JavaCode(@NotNull String name) {
        this.name = name;
        this.metrics = new ConcurrentHashMap<>();
        this.children = new ConcurrentHashMap<JavaCode, Boolean>().keySet(true);
    }

    public String getName() {
        return name;
    }

    public Stream<Metric> metrics() {
        Comparator<Metric> metricComparator = Comparator.comparing(m -> m.getType().description());
        return metrics.values().stream()
                .sorted(metricComparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaCode javaCode = (JavaCode) o;
        return Objects.equal(getName(), javaCode.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    public void addMetric(Metric metric) {
        metrics.put(metric.getType(), metric);
    }

    protected void addChild(JavaCode child) {
        children.add(child);
    }

    protected void accept(PsiElementVisitor visitor) {}

    @Nullable
    public Metric metric(MetricType metricType) {
        return metrics.get(metricType);
    }
}

