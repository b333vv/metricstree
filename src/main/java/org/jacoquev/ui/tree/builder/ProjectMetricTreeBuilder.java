package org.jacoquev.ui.tree.builder;

import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Sets;
import org.jacoquev.ui.tree.MetricsTreeFilter;
import org.jacoquev.ui.tree.node.*;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMetricTreeBuilder extends MetricTreeBuilder {

    public ProjectMetricTreeBuilder(JavaProject javaProject) {
        super(javaProject);
    }

    @Nullable
    public DefaultTreeModel createProjectMetricTreeModel() {
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
                        .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                        .forEach(m -> {
                            MetricNode metricNode = new ProjectMetricNode(m);
                            projectNode.add(metricNode);
                        });
            }

            if (getMetricsTreeFilter().isPackageMetricsVisible()
                    || getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                javaProject.getPackages()
                        .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
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
                .sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            parentNode.add(packageNode);

            addPackages(packageNode);

            if (getMetricsTreeFilter().isClassMetricsVisible()
                    || getMetricsTreeFilter().isMethodMetricsVisible()) {
                packageNode.getJavaPackage().getClasses()
                        .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
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
                        .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
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
