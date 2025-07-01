package org.b333vv.metric.service.impl;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.service.CalculationService;
import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.task.BuildMetricTreeTask;
import org.b333vv.metric.task.CategoryChartTask;
import org.b333vv.metric.task.MetricTreeMapTask;
import org.b333vv.metric.task.PieChartTask;

public class CalculationServiceImpl implements CalculationService {
    private final Project project;

    public CalculationServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public void calculateProjectTree() {
        project.getService(TaskQueueService.class).queue(new BuildMetricTreeTask(project));
    }

    @Override
    public void calculatePieChart() {
        project.getService(TaskQueueService.class).queue(new PieChartTask(project));
    }

    @Override
    public void calculateCategoryChart() {
        project.getService(TaskQueueService.class).queue(new CategoryChartTask(project));
    }

    @Override
    public void calculateMetricTreeMap() {
        project.getService(TaskQueueService.class).queue(new MetricTreeMapTask(project));
    }
}
