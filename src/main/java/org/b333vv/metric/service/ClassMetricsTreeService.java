package org.b333vv.metric.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressIndicator;
import org.b333vv.metric.builder.SortedClassesTreeModelCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;

import javax.swing.tree.DefaultTreeModel;

@Service(Service.Level.PROJECT)
public final class ClassMetricsTreeService {
    private final Project project;

    public ClassMetricsTreeService(Project project) {
        this.project = project;
    }

    public DefaultTreeModel getSortedClassesTreeModel(ProgressIndicator indicator) {
        CacheService cacheService = project.getService(CacheService.class);
        DefaultTreeModel treeModel = cacheService.getUserData(CacheService.CLASSES_BY_METRIC_TREE);

        if (treeModel == null) {
            JavaProject javaProject = project.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
            SortedClassesTreeModelCalculator calculator = new SortedClassesTreeModelCalculator();
            treeModel = calculator.calculate(javaProject, project);
            cacheService.putUserData(CacheService.CLASSES_BY_METRIC_TREE, treeModel);
        }
        return treeModel;
    }
}
