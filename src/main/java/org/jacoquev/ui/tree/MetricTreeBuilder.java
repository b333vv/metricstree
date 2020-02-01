package org.jacoquev.ui.tree;

import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Range;
import org.jacoquev.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricTreeBuilder {
    private DefaultTreeModel model;
    private JavaProject javaProject;

    public MetricTreeBuilder(JavaProject javaProject) {
        this.javaProject = javaProject;
    }

    public DefaultTreeModel createMetricTreeModel() {
        JavaPackage javaPackage = javaProject.getPackages().iterator().next();
        Iterator<JavaClass> typeIterator = javaPackage.getTypes().iterator();
        JavaClass rootJavaClass = typeIterator.next();
        ClassNode rootClassNode = new ClassNode(rootJavaClass);
        model = new DefaultTreeModel(rootClassNode);
        model.setRoot(rootClassNode);
        while (typeIterator.hasNext()) {
            JavaClass childJavaClass = typeIterator.next();
            ClassNode childClassNode = new ClassNode(childJavaClass);
            rootClassNode.add(childClassNode);
            addTypeMetricsAndMethodNodes(childClassNode);
        }
        addTypeMetricsAndMethodNodes(rootClassNode);
        return model;
    }

    private void addTypeMetricsAndMethodNodes(ClassNode classNode) {
        MetricsTreeFilter metricsTreeFilter = MetricsUtils.getMetricsTreeFilter();
        List<JavaMethod> sortedMethods = classNode.getJavaClass().getMethods().stream()
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).collect(Collectors.toList());
        for (JavaMethod javaMethod : sortedMethods) {
            MethodNode methodNode = new MethodNode(javaMethod);
            classNode.add(methodNode);
            if (metricsTreeFilter.isMethodMetricsVisible()) {
                addMethodMetricsNodes(methodNode);
            }
        }
        if (metricsTreeFilter.isClassMetricsVisible()) {
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

    private void addMethodMetricsNodes(MethodNode methodNode) {
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

    public JavaProject getJavaProject() {
        return javaProject;
    }

    private boolean mustBeShown(Metric metric) {
        MetricsTreeFilter metricsTreeFilter = MetricsUtils.getMetricsTreeFilter();
        return metricsTreeFilter.isAllowedValueMetricsVisible() && metric.hasAllowableValue() && metric.getRange() != Range.UNDEFINED_RANGE
                || metricsTreeFilter.isDisallowedValueMetricsVisible() && !metric.hasAllowableValue()
                || metricsTreeFilter.isNotSetValueMetricsVisible() && metric.getRange() == Range.UNDEFINED_RANGE;
    }
}
