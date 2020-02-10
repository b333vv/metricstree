package org.jacoquev.ui.tree.builder;

import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.ui.tree.MetricsTreeFilter;
import org.jacoquev.ui.tree.node.*;
import org.jacoquev.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMetricTreeBuilder extends MetricTreeBuilder {

    public ProjectMetricTreeBuilder(JavaProject javaProject) {
        super(javaProject);
    }

    public DefaultTreeModel createProjectMetricTreeModel() {
        ProjectNode projectNode = new ProjectNode(javaProject);
        model = new DefaultTreeModel(projectNode);
        model.setRoot(projectNode);
        List<JavaPackage> sortedPackages = javaProject.getPackages()
                .sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            projectNode.add(packageNode);
            addPackages(packageNode);
        }
        return model;
    }

    private void addPackages(PackageNode parentNode) {
        List<JavaPackage> sortedPackages = parentNode.getJavaPackage().getPackages()
                .sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            PackageNode packageNode = new PackageNode(javaPackage);
            parentNode.add(packageNode);
            addPackages(packageNode);
            List<JavaClass> sortedClasses = packageNode.getJavaPackage().getClasses()
                    .sorted((c1, c2) -> c1.getName().compareTo(c2.getName())).collect(Collectors.toList());
            for (JavaClass childJavaClass : sortedClasses) {
                ClassNode childClassNode = new ClassNode(childJavaClass);
                packageNode.add(childClassNode);
                addSubClasses(childClassNode);
                addTypeMetricsAndMethodNodes(childClassNode);
            }
        }
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getProjectMetricsTreeFilter();
    }
}
