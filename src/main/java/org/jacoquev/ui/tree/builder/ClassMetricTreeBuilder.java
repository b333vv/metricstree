package org.jacoquev.ui.tree.builder;

import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.ui.tree.MetricsTreeFilter;
import org.jacoquev.ui.tree.node.*;
import org.jacoquev.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;
import java.util.Iterator;

public class ClassMetricTreeBuilder extends MetricTreeBuilder {

    public ClassMetricTreeBuilder(JavaProject javaProject) {
        super(javaProject);
    }

    public DefaultTreeModel createMetricTreeModel() {
        if (getMetricsTreeFilter().isClassMetricsVisible()
            || getMetricsTreeFilter().isMethodMetricsVisible()) {
            JavaPackage javaPackage = javaProject.getPackages().findFirst().get();
            JavaClass rootJavaClass = javaPackage.getClasses().findFirst().get();
            ClassNode rootClassNode = new ClassNode(rootJavaClass);
            model = new DefaultTreeModel(rootClassNode);
            model.setRoot(rootClassNode);
            addSubClasses(rootClassNode);
            addTypeMetricsAndMethodNodes(rootClassNode);
            return model;
        } else {
            return null;
        }
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getClassMetricsTreeFilter();
    }
}
