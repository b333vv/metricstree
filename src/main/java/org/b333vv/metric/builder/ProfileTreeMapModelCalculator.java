package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.treemap.builder.TreeMapBuilder;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;

public class ProfileTreeMapModelCalculator {
    public MetricTreeMap<JavaCode> calculate(JavaProject javaProject) {
        TreeMapBuilder treeMapBuilder = new TreeMapBuilder();
        return treeMapBuilder.build(javaProject);
    }
}
