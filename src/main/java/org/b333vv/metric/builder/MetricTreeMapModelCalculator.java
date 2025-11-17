package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.treemap.builder.TreeMapBuilder;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;

public class MetricTreeMapModelCalculator {
    public MetricTreeMap<CodeElement> calculate(ProjectElement projectElement) {
        TreeMapBuilder treeMapBuilder = new TreeMapBuilder(projectElement);
        return treeMapBuilder.getTreeMap();
    }
}