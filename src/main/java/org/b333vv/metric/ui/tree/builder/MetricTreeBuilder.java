package org.b333vv.metric.ui.tree.builder;

import org.b333vv.metric.model.code.JavaClass;
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
import java.util.List;
import java.util.stream.Collectors;

public abstract class MetricTreeBuilder {
    protected DefaultTreeModel model;
    protected JavaProject javaProject;

    public MetricTreeBuilder(JavaProject javaProject) {
        this.javaProject = javaProject;
    }

    public JavaProject getJavaProject() {
        return javaProject;
    }

    public abstract DefaultTreeModel createMetricTreeModel();

    protected void addSubClasses(ClassNode parentClassNode) {
        List<JavaClass> sortedClasses = parentClassNode.getJavaClass().getClasses()
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName())).collect(Collectors.toList());
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
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).collect(Collectors.toList());
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
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).collect(Collectors.toList());
            for (Metric metric : sortedMetrics) {
                if (mustBeShown(metric) && checkClassMetricsSets(metric.getName())) {
                    MetricNode metricNode = new ClassMetricNode(metric);
                    classNode.add(metricNode);
                }
            }
        }
    }

    protected void addMethodMetricsNodes(MethodNode methodNode) {
        List<Metric> sortedMetrics = methodNode.getJavaMethod().getMetrics()
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).collect(Collectors.toList());
        for (Metric metric : sortedMetrics) {
            if (mustBeShown(metric)) {
                MetricNode metricNode = new MethodMetricNode(metric);
                methodNode.add(metricNode);
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
