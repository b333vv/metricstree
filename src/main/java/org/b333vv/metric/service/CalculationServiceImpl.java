package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;

import org.b333vv.metric.builder.ProjectTreeModelCalculator;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.task.ProjectTreeTask;
import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.util.SettingsService;

import javax.swing.tree.DefaultTreeModel;

public class CalculationServiceImpl implements CalculationService {
    private final Project project;
    private final TaskQueueService taskQueueService;
    private final CacheService cacheService;
    private final SettingsService settingsService;

    public CalculationServiceImpl(Project project) {
        this.project = project;
        this.taskQueueService = project.getService(TaskQueueService.class);
        this.cacheService = project.getService(CacheService.class);
        this.settingsService = project.getService(SettingsService.class);
    }

    @Override
    public void calculateProjectTree() {
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearProjectMetricsTree();

        DefaultTreeModel treeModel = cacheService.getUserData(CacheService.PROJECT_TREE);

        if (treeModel != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsTreeIsReady(treeModel);
        } else {
            ProjectTreeModelCalculator calculator = new ProjectTreeModelCalculator(project);
            ProjectTreeTask task = new ProjectTreeTask(project, () -> calculator.calculate(), cacheService);
            taskQueueService.queue(task);
        }
    }
}
