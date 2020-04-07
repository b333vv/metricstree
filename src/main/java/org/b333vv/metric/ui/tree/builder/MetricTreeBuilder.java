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

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.Sets;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MetricTreeBuilder {
    protected DefaultTreeModel model;
    protected final JavaProject javaProject;

    public MetricTreeBuilder(JavaProject javaProject) {
        this.javaProject = javaProject;
    }

    public JavaProject getJavaProject() {
        return javaProject;
    }

    public abstract DefaultTreeModel createMetricTreeModel();

    protected void addSubClasses(ClassNode parentClassNode) {
        List<JavaClass> sortedClasses = parentClassNode.getJavaClass().getClasses()
                .sorted(Comparator.comparing(JavaCode::getName)).collect(Collectors.toList());
        for (JavaClass javaClass : sortedClasses) {
            ClassNode classNode = new ClassNode(javaClass);
            parentClassNode.add(classNode);
            addSubClasses(classNode);
            addTypeMetricsAndMethodNodes(classNode);
        }
    }

    protected void addTypeMetricsAndMethodNodes(ClassNode classNode) {
        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
            List<JavaMethod> sortedMethods = classNode.getJavaClass().getMethods()
                    .sorted(Comparator.comparing(JavaCode::getName)).collect(Collectors.toList());
            for (JavaMethod javaMethod : sortedMethods) {
                MethodNode methodNode = new MethodNode(javaMethod);
                classNode.add(methodNode);
                if (getMetricsTreeFilter().isMethodMetricsVisible()) {
                    addMethodMetricsNodes(methodNode);
                }
            }
        }
        if (getMetricsTreeFilter().isClassMetricsVisible()) {
            List<Metric> sortedMetrics = classNode.getJavaClass().getMetrics()
                    .sorted(Comparator.comparing(Metric::getName)).collect(Collectors.toList());
            for (Metric metric : sortedMetrics) {
                if (mustBeShown(metric) && checkClassMetricsSets(metric.getName())) {
                    MetricNode metricNode = new ClassMetricNode(metric);
                    classNode.add(metricNode);
                    storeMetric(classNode, metricNode);
                }
            }
        }
    }

    protected void storeMetric(ClassNode classNode, MetricNode metricNode) {}
    protected void storeMetric(MethodNode methodNode, MetricNode metricNode) {}

    protected void addMethodMetricsNodes(MethodNode methodNode) {
        List<Metric> sortedMetrics = methodNode.getJavaMethod().getMetrics()
                .sorted(Comparator.comparing(Metric::getName)).collect(Collectors.toList());
        for (Metric metric : sortedMetrics) {
            if (mustBeShown(metric)) {
                MetricNode metricNode = new MethodMetricNode(metric);
                methodNode.add(metricNode);
                storeMetric(methodNode, metricNode);
            }
        }
    }

    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getClassMetricsTreeFilter();
    }

    protected boolean mustBeShown(Metric metric) {
        MetricsTreeFilter metricsTreeFilter = getMetricsTreeFilter();
        return metricsTreeFilter.isAllowedValueMetricsVisible()
                    && metric.hasAllowableValue()
                    && metric.getRange() != Range.UNDEFINED
                    && metric.getValue() != Value.UNDEFINED
                || metricsTreeFilter.isDisallowedValueMetricsVisible()
                    && !metric.hasAllowableValue()
                    && metric.getValue() != Value.UNDEFINED
                || metricsTreeFilter.isNotSetValueMetricsVisible()
                    && metric.getRange() == Range.UNDEFINED
                    && metric.getValue() != Value.UNDEFINED
                || metricsTreeFilter.isNotApplicableMetricsVisible()
                    && metric.getValue() == Value.UNDEFINED;
    }

    protected boolean checkClassMetricsSets(String metricName) {
        MetricsTreeFilter metricsTreeFilter = getMetricsTreeFilter();
        return metricsTreeFilter.isChidamberKemererMetricsSetVisible() && Sets.inChidamberKemererMetricsSet(metricName)
                || metricsTreeFilter.isLorenzKiddMetricsSetVisible() && Sets.inLorenzKiddMetricsSet(metricName)
                || metricsTreeFilter.isLiHenryMetricsSetVisible() && Sets.inLiHenryMetricsSet(metricName);
    }
}
