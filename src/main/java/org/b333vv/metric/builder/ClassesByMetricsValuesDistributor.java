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

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

public class ClassesByMetricsValuesDistributor {
    public static Map<MetricType, Map<JavaClass, Metric>> classesByMetricsValuesDistribution(JavaProject javaProject) {
        return javaProject.allClasses().flatMap(
                inner -> inner.metrics()
                        .filter(metric -> MetricsService.isLongValueMetricType(metric.getType())
                                && MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) != RangeType.UNDEFINED)
                        .collect(groupingBy(Metric::getType, groupingBy(i -> inner)))
                        .entrySet()
                        .stream())
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, new SortedByMetricsValuesClassesCollector())));
    }

    private static class SortedByMetricsValuesClassesCollector
            implements Collector<Map<JavaClass, List<Metric>>, Map<JavaClass, Metric>, Map<JavaClass, Metric>>
    {

        @Override
        public Supplier<Map<JavaClass, Metric>> supplier() {
            return LinkedHashMap::new;
        }

        @Override
        public BiConsumer<Map<JavaClass, Metric>, Map<JavaClass, List<Metric>>> accumulator() {
            return (map, val) -> {
                Map.Entry<JavaClass, List<Metric>> entry = val.entrySet().iterator().next();
                JavaClass javaClass = entry.getKey();
                Metric metric = entry.getValue().get(0);
                map.put(javaClass, metric);
            };
        }

        @Override
        public Function<Map<JavaClass, Metric>, Map<JavaClass, Metric>> finisher() {
            return MetricsUtils::sortByValueReversed;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }

        @Override
        public BinaryOperator<Map<JavaClass, Metric>> combiner() {
            return (map1, map2) -> {
                map2.forEach(map1::put);
                return map1;
            };
        }
    }
}
