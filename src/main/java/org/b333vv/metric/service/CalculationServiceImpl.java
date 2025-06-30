package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;

import org.b333vv.metric.builder.ProjectTreeModelCalculator;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.task.ProjectTreeTask;
import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.util.SettingsService;

import javax.swing.tree.DefaultTreeModel;

import org.b333vv.metric.builder.PieChartDataCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;
import org.b333vv.metric.task.PieChartTask;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.task.CategoryChartTask;
import org.knowm.xchart.CategoryChart;

import java.util.List;

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

    @Override
    public void calculatePieChart() {
        List<MetricPieChartBuilder.PieChartStructure> pieChartList = cacheService.getUserData(CacheService.PIE_CHART_LIST);
        if (pieChartList != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).pieChartIsReady();
        } else {
            PieChartTask task = new PieChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateCategoryChart() {
        CategoryChart categoryChart = cacheService.getUserData(CacheService.CATEGORY_CHART);
        if (categoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).categoryChartIsReady();
        } else {
            CategoryChartTask task = new CategoryChartTask(project);
            taskQueueService.queue(task);
        }
    }
}
