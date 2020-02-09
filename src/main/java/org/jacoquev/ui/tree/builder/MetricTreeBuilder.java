package org.jacoquev.ui.tree.builder;

import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Range;
import org.jacoquev.ui.tree.MetricsTreeFilter;
import org.jacoquev.ui.tree.node.*;
import org.jacoquev.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;
import java.util.List;
import java.util.Set;
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

    protected void addSubClasses(ClassNode parentClassNode) {
        List<JavaClass> sortedClasses = parentClassNode.getJavaClass().getClasses().stream()
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName())).collect(Collectors.toList());
        for (JavaClass javaClass : sortedClasses) {
            ClassNode classNode = new ClassNode(javaClass);
            parentClassNode.add(classNode);
            addSubClasses(classNode);
            addTypeMetricsAndMethodNodes(classNode);
        }
    }

    protected void addTypeMetricsAndMethodNodes(ClassNode classNode) {
        List<JavaMethod> sortedMethods = classNode.getJavaClass().getMethods().stream()
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).collect(Collectors.toList());
        for (JavaMethod javaMethod : sortedMethods) {
            MethodNode methodNode = new MethodNode(javaMethod);
            classNode.add(methodNode);
            if (getMetricsTreeFilter().isMethodMetricsVisible()) {
                addMethodMetricsNodes(methodNode);
            }
        }
        if (getMetricsTreeFilter().isClassMetricsVisible()) {
            Set<Metric> metrics = classNode.getJavaClass().getMetrics();
            List<Metric> sortedMetrics = metrics.stream()
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).collect(Collectors.toList());
            for (Metric metric : sortedMetrics) {
                if (mustBeShown(metric)) {
                    MetricNode metricNode = new ClassMetricNode(metric);
                    classNode.add(metricNode);
                }
            }
        }
    }

    protected void addMethodMetricsNodes(MethodNode methodNode) {
        Set<Metric> metrics = methodNode.getJavaMethod().getMetrics();
        List<Metric> sortedMetrics = metrics.stream()
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
        return metricsTreeFilter.isAllowedValueMetricsVisible() && metric.hasAllowableValue() && metric.getRange() != Range.UNDEFINED_RANGE
                || metricsTreeFilter.isDisallowedValueMetricsVisible() && !metric.hasAllowableValue()
                || metricsTreeFilter.isNotSetValueMetricsVisible() && metric.getRange() == Range.UNDEFINED_RANGE;
    }
}
