package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.service.CalculationService;

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
        
        // Ensure the project metrics are built before creating the tree
        CalculationService calculationService = project.getService(CalculationService.class);
        ProjectElement projectElement;
        if (calculationService instanceof org.b333vv.metric.service.CalculationServiceImpl) {
            projectElement = ((org.b333vv.metric.service.CalculationServiceImpl) calculationService).getOrBuildProjectMetricsModel(indicator);
        } else {
            // Fallback to cache service for compatibility
            projectElement = project.getService(CacheService.class).getProject();
        }
        
        if (projectElement == null) {
            throw new IllegalStateException("projectElement is null - metrics calculation may have failed");
        }
        
        ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(projectElement, project);
        return projectMetricTreeBuilder.createMetricTreeModel();
    }
}
