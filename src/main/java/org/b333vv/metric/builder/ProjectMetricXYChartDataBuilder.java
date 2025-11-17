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

package org.b333vv.metric.builder;

import com.intellij.psi.PsiPackage;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class ProjectMetricXYChartDataBuilder {

    // XChart requires non-null, non-empty series names. Use a safe default when needed.
    private static final String DEFAULT_PACKAGE_SERIES_NAME = "(default)";

    public static void build(ProjectElement projectElement, Map<String, Double> instability, Map<String, Double> abstractness) {
        Map<String, Double> myInstability = projectElement.allPackages()
                .flatMap(
                        inner -> inner.metrics()
                                .filter(metric -> metric.getType() == MetricType.I)
                                .collect(groupingBy(i -> inner))
                                .entrySet()
                                .stream()
                                .map(ProjectMetricXYChartDataBuilder::convert))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, TreeMap::new));

        Map<String, Double> myAbstractness = projectElement.allPackages()
                .flatMap(
                        inner -> inner.metrics()
                                .filter(metric -> metric.getType() == MetricType.A)
                                .collect(groupingBy(i -> inner))
                                .entrySet()
                                .stream()
                                .map(ProjectMetricXYChartDataBuilder::convert))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, TreeMap::new));

        instability.putAll(myInstability);
        abstractness.putAll(myAbstractness);
    }

    private static Map.Entry<String, Double> convert(Map.Entry<PackageElement, List<Metric>> e) {
        String packageName = safeSeriesName(e.getKey());
        // Given the upstream grouping, there should be at least one metric; still guard defensively
        Double value = e.getValue().isEmpty()
                ? Double.NaN
                : e.getValue().get(0).getPsiValue().doubleValue();

        // Immutable entry is fine here
        return Map.entry(packageName, value);
    }

    private static String safeSeriesName(PackageElement pkg) {
        // Prefer the qualified name from PsiPackage when available and non-empty; otherwise fallback
        PsiPackage psiPackage = pkg.getPsiPackage();
        String qName = psiPackage != null ? psiPackage.getQualifiedName() : null;

        if (qName != null) {
            qName = qName.trim();
            if (!qName.isEmpty()) {
                return qName;
            }
        }

        String name = pkg.getName();
        if (name != null) {
            name = name.trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        return DEFAULT_PACKAGE_SERIES_NAME;
    }
}