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
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricSet;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public abstract class MetricTreeBuilder {
    protected DefaultTreeModel model;
    protected final JavaCode javaCode;

    public MetricTreeBuilder(JavaCode javaCode) {
        this.javaCode = javaCode;
    }

    public JavaCode getJavaCode() {
        return javaCode;
    }

    public abstract DefaultTreeModel createMetricTreeModel();

    protected void addSubClasses(ClassNode parentClassNode) {
        parentClassNode.getJavaClass().innerClasses()
                .map(ClassNode::new)
                .forEach(c -> {
                    parentClassNode.add(c);
                    addSubClasses(c);
                    addTypeMetricsAndMethodNodes(c);
                });
    }

    protected void addTypeMetricsAndMethodNodes(ClassNode classNode) {
        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
            classNode.getJavaClass().methods()
                    .map(MethodNode::new)
                    .forEach(m -> {
                        classNode.add(m);
                        if (getMetricsTreeFilter().isMethodMetricsVisible()) {
                            addMethodMetricsNodes(m);
                        }});
        }
        if (getMetricsTreeFilter().isClassMetricsVisible()) {
            classNode.getJavaClass().metrics()
                    .filter(m -> mustBeShown(m) && checkClassMetricsSets(m.getType()))
                    .map(ClassMetricNode::new)
                    .forEach(m -> {
                            classNode.add(m);
                            storeMetric(classNode, m);
                    });
        }
    }

    protected void addMethodMetricsNodes(MethodNode methodNode) {
        methodNode.getJavaMethod().metrics()
                .filter(this::mustBeShown)
                .map(MethodMetricNode::new)
                .forEach(m -> {
                    methodNode.add(m);
                    storeMetric(methodNode, m);
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
                    && metric.getValue() != Value.UNDEFINED
                    && MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.REGULAR
                || metricsTreeFilter.isDisallowedValueMetricsVisible()
                    && metric.getValue() != Value.UNDEFINED
                    && MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) != RangeType.REGULAR
                    && MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) != RangeType.UNDEFINED
                || metricsTreeFilter.isNotSetValueMetricsVisible()
                    && metric.getValue() != Value.UNDEFINED
                    && MetricsService.getRangeForMetric(metric.getType()).getRangeType(metric.getValue()) == RangeType.UNDEFINED
                || metricsTreeFilter.isNotApplicableMetricsVisible()
                    && metric.getValue() == Value.UNDEFINED;
    }

    protected boolean checkClassMetricsSets(MetricType type) {
        MetricsTreeFilter metricsTreeFilter = getMetricsTreeFilter();
        return metricsTreeFilter.isChidamberKemererMetricsSetVisible() && type.set() == MetricSet.CHIDAMBER_KEMERER
                || metricsTreeFilter.isLorenzKiddMetricsSetVisible() && type.set() == MetricSet.LORENZ_KIDD
                || metricsTreeFilter.isLiHenryMetricsSetVisible() && type.set() == MetricSet.LI_HENRY
                || type.set() == MetricSet.BIEMAN_KANG
                || metricsTreeFilter.isLanzaMarinescuMetricsSetVisible() && type.set() == MetricSet.LANZA_MARINESCU
                || type.set() == MetricSet.CLEMENS_LEE
                || type.set() == MetricSet.HALSTEAD_CLASS;
    }
}
