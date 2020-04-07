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

import com.intellij.vcs.log.TimedVcsCommit;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.node.ClassNode;
import org.b333vv.metric.ui.tree.node.MethodNode;
import org.b333vv.metric.ui.tree.node.MetricHistoryNode;
import org.b333vv.metric.ui.tree.node.MetricNode;

import javax.swing.tree.DefaultTreeModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClassMetricsValuesEvolutionTreeBuilder extends ClassMetricTreeBuilder {

    private final Map<String, MetricNode> classesAndMethodsMetrics = new HashMap<>();
    private final Map<TimedVcsCommit, JavaClass> classMetricsEvolution;
    private final Map<String, Value> previousValue = new HashMap<>();

    public ClassMetricsValuesEvolutionTreeBuilder(JavaProject javaProject,
                                                  Map<TimedVcsCommit, JavaClass> classMetricsEvolution) {
        super(javaProject);
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
                                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm:ss"));
                        String hash = timedMetricsSet.getKey().getId().toShortString();
                        JavaClass javaClass = timedMetricsSet.getValue();
                        buildMetricsNodesForClasses(javaClass, hash, dateTime);
            });
        return model;
    }

    private void buildMetricsNodesForClasses(JavaClass javaClass, String hash, String dateTime) {
        javaClass.getMetrics().forEach(m -> {
            MetricHistoryNode historyNode =
                    new MetricHistoryNode(dateTime,
                            hash, m, previousValue
                            .getOrDefault(getKeyForClass(javaClass, m), Value.UNDEFINED));
            String key = getKeyForClass(javaClass, m);
            if (classesAndMethodsMetrics.get(key) != null) {
                classesAndMethodsMetrics.get(key).insert(historyNode, 0);
                previousValue.put(getKeyForClass(javaClass, m), m.getValue());
            }
        });
        javaClass.getClasses().forEach(c -> buildMetricsNodesForClasses(c, hash, dateTime));
        javaClass.getMethods().forEach(m -> buildMetricsNodesForMethods(m, hash, dateTime));
    }

    private void buildMetricsNodesForMethods(JavaMethod javaMethod, String hash, String dateTime) {
        javaMethod.getMetrics().forEach(m -> {
            MetricHistoryNode historyNode =
                    new MetricHistoryNode(dateTime,
                            hash, m,
                            previousValue.getOrDefault(getKeyForMethod(javaMethod, m), Value.UNDEFINED));
            String key = getKeyForMethod(javaMethod, m);
            if (classesAndMethodsMetrics.get(key) != null) {
                classesAndMethodsMetrics.get(key).insert(historyNode, 0);
                previousValue.put(getKeyForMethod(javaMethod, m), m.getValue());
            }
        });
    }

    private String getKeyForClass(JavaClass javaClass, Metric metric) {
        return javaClass.getPsiClass().getQualifiedName() + ":" + metric.getName();
    }

    private String getKeyForMethod(JavaMethod javaMethod, Metric metric) {
        return Objects.requireNonNull(javaMethod.getPsiMethod().getContainingClass()).getQualifiedName()
                + ":" + JavaMethod.signature(javaMethod.getPsiMethod()) + ":" + metric.getName();
    }
}