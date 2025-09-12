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
    public static void build(ProjectElement javaProject, Map<String, Double> instability, Map<String, Double> abstractness) {
        Map<String, Double> myInstability = javaProject.allPackages()
            .flatMap(
                inner -> inner.metrics()
                        .filter(metric -> metric.getType() == MetricType.I)
                        .collect(groupingBy(i -> inner))
                        .entrySet()
                        .stream()
                        .map(ProjectMetricXYChartDataBuilder::convert))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, TreeMap::new));

        Map<String, Double> myAbstractness = javaProject.allPackages()
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
        String packageName = e.getKey().getPsiPackage().getQualifiedName();
        Double value = e.getValue().get(0).getPsiValue().doubleValue();
        return new Map.Entry<>() {
            @Override
            public String getKey() {
                return packageName;
            }

            @Override
            public Double getValue() {
                return value;
            }

            @Override
            public Double setValue(Double value1) {
                return null;
            }
        };
    }
}
