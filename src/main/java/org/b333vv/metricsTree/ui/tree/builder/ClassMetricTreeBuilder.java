package org.b333vv.metricsTree.ui.tree.builder;

import org.b333vv.metricsTree.model.code.JavaClass;
import org.b333vv.metricsTree.model.code.JavaPackage;
import org.b333vv.metricsTree.model.code.JavaProject;
import org.b333vv.metricsTree.ui.tree.MetricsTreeFilter;
import org.b333vv.metricsTree.ui.tree.node.ClassNode;
import org.b333vv.metricsTree.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

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
