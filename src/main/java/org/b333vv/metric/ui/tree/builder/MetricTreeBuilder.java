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

import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.Sets;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public abstract class MetricTreeBuilder {
    protected DefaultTreeModel model;
    protected final JavaCode javaCode;

    public MetricTreeBuilder(JavaCode javaCode) {
        this.javaCode = javaCode;
    }

    public abstract DefaultTreeModel createMetricTreeModel();

    protected void addSubClasses(ClassNode parentClassNode) {
        parentClassNode.getJavaClass().getClasses()
                .forEach(c -> {
                    ClassNode classNode = new ClassNode(c);
                    parentClassNode.add(classNode);
                    addSubClasses(classNode);
                    addTypeMetricsAndMethodNodes(classNode);
                });
    }

    protected void addTypeMetricsAndMethodNodes(ClassNode classNode) {
        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
            classNode.getJavaClass().getMethods()
                    .forEach(m -> {
                        MethodNode methodNode = new MethodNode(m);
                        classNode.add(methodNode);
                        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
                            addMethodMetricsNodes(methodNode);
                        }});
        }
        if (getMetricsTreeFilter().isClassMetricsVisible()) {
            classNode.getJavaClass().getMetrics()
                    .forEach(m -> {
                        if (mustBeShown(m) && checkClassMetricsSets(m.getType())) {
                            MetricNode metricNode = new ClassMetricNode(m);
                            classNode.add(metricNode);
                            storeMetric(classNode, metricNode);
                        }
                    });
        }
    }

    protected void addMethodMetricsNodes(MethodNode methodNode) {
        methodNode.getJavaMethod().getMetrics()
                .forEach(m -> {
                    if (mustBeShown(m)) {
                        MetricNode metricNode = new MethodMetricNode(m);
                        methodNode.add(metricNode);
                        storeMetric(methodNode, metricNode);
                    }
                });
    }

    protected void storeMetric(ClassNode classNode, MetricNode metricNode) {}

    protected void storeMetric(MethodNode methodNode, MetricNode metricNode) {}

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

    protected boolean checkClassMetricsSets(MetricType type) {
        MetricsTreeFilter metricsTreeFilter = getMetricsTreeFilter();
        return metricsTreeFilter.isChidamberKemererMetricsSetVisible() && type.set() == MetricSet.CHIDAMBER_KEMERER
                || metricsTreeFilter.isLorenzKiddMetricsSetVisible() && type.set() == MetricSet.LORENZ_KIDD
                || metricsTreeFilter.isLiHenryMetricsSetVisible() && type.set() == MetricSet.LI_HENRY;
    }
}
