package org.b333vv.metric.ui.tree.builder;

import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.ui.tree.MetricsTreeFilter;
import org.b333vv.metric.ui.tree.node.*;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMetricTreeBuilder extends MetricTreeBuilder {

    public ProjectMetricTreeBuilder(JavaProject javaProject) {
        super(javaProject);
    }

    @Nullable
    public DefaultTreeModel createMetricTreeModel() {
        if (getMetricsTreeFilter().isProjectMetricsVisible()
                || getMetricsTreeFilter().isPackageMetricsVisible()
                || getMetricsTreeFilter().isClassMetricsVisible()
                || getMetricsTreeFilter().isMethodMetricsVisible()) {
            ProjectNode projectNode = new ProjectNode(javaProject);
            model = new DefaultTreeModel(projectNode);
            model.setRoot(projectNode);

            if (getMetricsTreeFilter().isProjectMetricsVisible()
                    && getMetricsTreeFilter().isMoodMetricsSetVisible()) {
                javaProject.getMetrics()
                        .sorted(Comparator.comparing(Metric::getName))
                        .forEach(m -> {
                            MetricNode metricNode = new ProjectMetricNode(m);
                            projectNode.add(metricNode);
                        });
            }

            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    || getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                javaProject.getPackages()
                        .sorted(Comparator.comparing(JavaCode::getName))
                        .map(PackageNode::new).forEach(packageNode -> {
                    projectNode.add(packageNode);
                    addPackages(packageNode);
                });
            }
            return model;
        } else {
            return null;
        }
    }

    private void addPackages(PackageNode parentNode) {
        List<JavaPackage> sortedPackages = parentNode.getJavaPackage().getPackages()
                .sorted(Comparator.comparing(JavaCode::getName)).collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            parentNode.add(packageNode);

            addPackages(packageNode);

            if (getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                packageNode.getJavaPackage().getClasses()
                        .sorted(Comparator.comparing(JavaCode::getName))
                        .forEach(c -> {
                            ClassNode childClassNode = new ClassNode(c);
                            packageNode.add(childClassNode);
                            addSubClasses(childClassNode);
                            addTypeMetricsAndMethodNodes(childClassNode);
                        });
            }

            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    && getMetricsTreeFilter().isRobertMartinMetricsSetVisible()) {
                javaPackage.getMetrics()
                        .sorted(Comparator.comparing(Metric::getName))
                        .filter(this::mustBeShown)
                        .forEach(m -> {
                            MetricNode metricNode = new PackageMetricNode(m);
                            packageNode.add(metricNode);
                        });
            }
        }
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getProjectMetricsTreeFilter();
    }
}
