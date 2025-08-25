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

package org.b333vv.metric.ui.tree.builder;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.TimedVcsCommit;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.node.ClassNode;
import org.b333vv.metric.ui.tree.node.MethodNode;
import org.b333vv.metric.ui.tree.node.MetricHistoryNode;
import org.b333vv.metric.ui.tree.node.MetricNode;

import javax.swing.tree.DefaultTreeModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class ClassMetricsValuesEvolutionTreeBuilder extends ClassMetricTreeBuilder {

    private final Map<EvolutionKey, MetricNode> classesAndMethodsMetrics = new HashMap<>();
    private final Map<TimedVcsCommit, Set<JavaClass>> classMetricsEvolution;
    private final Map<EvolutionKey, Value> previousValue = new HashMap<>();

    public ClassMetricsValuesEvolutionTreeBuilder(JavaFile javaFile,
                                                  Map<TimedVcsCommit, Set<JavaClass>> classMetricsEvolution,
                                                  Project project) {
        super(javaFile, project);
        this.classMetricsEvolution = classMetricsEvolution;
    }

    @Override
    protected boolean mustBeShown(Metric metric) {
        return true;
    }

    @Override
    protected void storeMetric(ClassNode classNode, MetricNode metricNode) {
        classesAndMethodsMetrics.put(getKeyForClass(classNode.getJavaClass(), metricNode.getMetric()), metricNode);
    }

    @Override
    protected void storeMetric(MethodNode methodNode, MetricNode metricNode) {
        classesAndMethodsMetrics.put(getKeyForMethod(methodNode.getJavaMethod(), metricNode.getMetric()), metricNode);
    }

    public DefaultTreeModel createMetricsValuesEvolutionTreeModel() {
        DefaultTreeModel model = createMetricTreeModel();
        classMetricsEvolution.entrySet().stream()
                .sorted(Comparator.comparingLong(es -> es.getKey().getTimestamp()))
                .forEach(timedMetricsSet -> {
                        String dateTime = LocalDateTime
                                .ofInstant(Instant.ofEpochMilli(timedMetricsSet.getKey().getTimestamp()),
                                        TimeZone.getDefault().toZoneId())
                                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
                        String hash = timedMetricsSet.getKey().getId().toShortString();
                        timedMetricsSet.getValue().forEach(c -> buildMetricsNodesForClasses(c, hash, dateTime));
            });
        return model;
    }

    private void buildMetricsNodesForClasses(JavaClass javaClass, String hash, String dateTime) {
        javaClass.metrics().forEach(m -> handleMetric(hash, dateTime, m, getKeyForClass(javaClass, m)));
        javaClass.innerClasses().forEach(c -> buildMetricsNodesForClasses(c, hash, dateTime));
        javaClass.methods().forEach(m -> buildMetricsNodesForMethods(m, hash, dateTime));
    }

    private void buildMetricsNodesForMethods(JavaMethod javaMethod, String hash, String dateTime) {
        javaMethod.metrics().forEach(m -> handleMetric(hash, dateTime, m, getKeyForMethod(javaMethod, m)));
    }

    private void handleMetric(String hash, String dateTime, Metric m, EvolutionKey key) {
        MetricHistoryNode historyNode = new MetricHistoryNode(dateTime, hash, m, previousValue.getOrDefault(key, Value.UNDEFINED), project);
        if (classesAndMethodsMetrics.get(key) != null) {
            classesAndMethodsMetrics.get(key).insert(historyNode, 0);
            previousValue.put(key, m.getPsiValue());
        }
    }

    private EvolutionKey getKeyForClass(JavaClass javaClass, Metric metric) {
        return ReadAction.compute(() -> 
            new EvolutionKey(javaClass.getPsiClass().getQualifiedName(), metric.getType())
        );
    }

    private EvolutionKey getKeyForMethod(JavaMethod javaMethod, Metric metric) {
        return ReadAction.compute(() -> 
            new EvolutionKey(Objects.requireNonNull(javaMethod.getPsiMethod().getContainingClass()).getQualifiedName(),
                    JavaMethod.signature(javaMethod.getPsiMethod()),
                    metric.getType())
        );
    }

    private static class EvolutionKey {
        private final String className;
        private final String methodName;
        private final MetricType metricType;

        public EvolutionKey(String className, MetricType metricType) {
            this.className = className;
            this.methodName = "";
            this.metricType = metricType;
        }

        public EvolutionKey(String className, String methodName, MetricType metricType) {
            this.className = className;
            this.methodName = methodName;
            this.metricType = metricType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EvolutionKey)) return false;
            EvolutionKey that = (EvolutionKey) o;
            return className.equals(that.className) &&
                    methodName.equals(that.methodName) &&
                    metricType == that.metricType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, metricType);
        }
    }
}