package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.tree.builder.SortedByMetricsValuesClassesTreeBuilder;

import javax.swing.tree.DefaultTreeModel;

public class SortedClassesTreeModelCalculator {

    public DefaultTreeModel calculate(ProjectElement javaProject, Project project) {
        SortedByMetricsValuesClassesTreeBuilder builder = new SortedByMetricsValuesClassesTreeBuilder();
        return builder.createMetricTreeModel(javaProject, project);
    }
}
