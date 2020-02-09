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

    public DefaultTreeModel createClassMetricTreeModel() {
        JavaPackage javaPackage = javaProject.getPackages().iterator().next();
        Iterator<JavaClass> typeIterator = javaPackage.getClasses().iterator();
        JavaClass rootJavaClass = typeIterator.next();
        ClassNode rootClassNode = new ClassNode(rootJavaClass);
        model = new DefaultTreeModel(rootClassNode);
        model.setRoot(rootClassNode);
        addSubClasses(rootClassNode);
        addTypeMetricsAndMethodNodes(rootClassNode);
        return model;
    }

    @Override
    protected MetricsTreeFilter getMetricsTreeFilter() {
        return MetricsUtils.getClassMetricsTreeFilter();
    }
}
