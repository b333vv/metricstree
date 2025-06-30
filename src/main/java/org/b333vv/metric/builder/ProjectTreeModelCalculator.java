package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;

import javax.swing.tree.DefaultTreeModel;

public class ProjectTreeModelCalculator {
    private final Project project;

    public ProjectTreeModelCalculator(Project project) {
        this.project = project;
    }

    public DefaultTreeModel calculate() {
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        JavaProject javaProject = project.getService(MetricTaskManager.class).getProjectModel(indicator);
        ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject, project);
        return projectMetricTreeBuilder.createMetricTreeModel();
    }
}
