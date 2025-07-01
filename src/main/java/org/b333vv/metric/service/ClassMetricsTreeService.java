package org.b333vv.metric.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressIndicator;
import org.b333vv.metric.builder.SortedClassesTreeModelCalculator;
import org.b333vv.metric.model.code.JavaProject;


import javax.swing.tree.DefaultTreeModel;

@Service(Service.Level.PROJECT)
public final class ClassMetricsTreeService {
    private final Project project;
    private final CalculationService calculationService;

    public ClassMetricsTreeService(Project project) {
        this.project = project;
        this.calculationService = project.getService(CalculationService.class);
    }

    public DefaultTreeModel getSortedClassesTreeModel(ProgressIndicator indicator) {
        CacheService cacheService = project.getService(CacheService.class);
        DefaultTreeModel treeModel = cacheService.getUserData(CacheService.CLASSES_BY_METRIC_TREE);

        if (treeModel == null) {
            JavaProject javaProject = calculationService.getOrBuildClassAndMethodModel(indicator);
            SortedClassesTreeModelCalculator calculator = new SortedClassesTreeModelCalculator();
            treeModel = calculator.calculate(javaProject, project);
            cacheService.putUserData(CacheService.CLASSES_BY_METRIC_TREE, treeModel);
        }
        return treeModel;
    }
}
