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

package org.b333vv.metric.exec;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.b333vv.metric.model.metric.value.RangeType.*;

public class ClassesByMetricsValuesCounter {
    private static long numberOfClasses;
    public static Map<MetricType, Map<RangeType, Double>> classesByMetricsValuesDistribution(JavaProject javaProject) {
        numberOfClasses = javaProject.allClasses().count();
        return javaProject.allClasses().flatMap(
                inner -> inner.metrics()
                        .filter(metric -> MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) != RangeType.UNDEFINED)
                        .collect(groupingBy(Metric::getType, groupingBy(i -> inner)))
                        .entrySet()
                        .stream())
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, new ClassesByRangeTypeDistributionCollector())));
    }

    private static class ClassesByRangeTypeDistributionCollector
            implements Collector<Map<JavaClass, List<Metric>>, Map<RangeType, Long>, Map<RangeType, Double>>
    {

        @Override
        public Supplier<Map<RangeType, Long>> supplier() {
            return () -> new EnumMap<>(RangeType.class);
        }

        @Override
        public BiConsumer<Map<RangeType, Long>, Map<JavaClass, List<Metric>>> accumulator() {
            return (map, val) -> {
                Map.Entry<JavaClass, List<Metric>> entry = val.entrySet().iterator().next();
                Metric metric = entry.getValue().get(0);
                RangeType rangeType = MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue());
                map.merge(rangeType, 1L, Long::sum);
            };
        }

        @Override
        public Function<Map<RangeType, Long>, Map<RangeType, Double>> finisher() {
            return ClassesByMetricsValuesCounter::makeValuesRelative;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }

        @Override
        public BinaryOperator<Map<RangeType, Long>> combiner() {
            return (map1, map2) -> {
                map2.forEach(map1::put);
                return map1;
            };
        }
    }
    private static Map<RangeType, Double> makeValuesRelative(Map<RangeType, Long> map) {
        Map<RangeType, Double> classesPercentsByRangeType = new EnumMap<>(RangeType.class);
        for (Map.Entry<RangeType, Long> entry : map.entrySet()) {
            classesPercentsByRangeType.put(entry.getKey(), (double) entry.getValue() / (double) numberOfClasses);
        }
        return classesPercentsByRangeType;
    }
}
