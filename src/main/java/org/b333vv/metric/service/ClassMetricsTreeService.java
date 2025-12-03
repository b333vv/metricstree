package org.b333vv.metric.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressIndicator;
import org.b333vv.metric.builder.SortedClassesTreeModelCalculator;
import org.b333vv.metric.model.code.ProjectElement;

import javax.swing.tree.DefaultTreeModel;

@Service(Service.Level.PROJECT)
public final class ClassMetricsTreeService {
    private final Project project;
    private final CalculationService calculationService;

    public ClassMetricsTreeService(Project project) {
        this.project = project;
        this.calculationService = project.getService(CalculationService.class);
    }

    public DefaultTreeModel getSortedClassesTreeModel(ProgressIndicator indicator,
            @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        CacheService cacheService = project.getService(CacheService.class);
        DefaultTreeModel treeModel = cacheService.getClassesByMetricTree(module);

        if (treeModel == null) {
            ProjectElement projectElement = calculationService.getOrBuildClassAndMethodModel(indicator, module);
            SortedClassesTreeModelCalculator calculator = new SortedClassesTreeModelCalculator();
            treeModel = calculator.calculate(projectElement, project);
            cacheService.putClassesByMetricTree(module, treeModel);
        }
        return treeModel;
    }
}
