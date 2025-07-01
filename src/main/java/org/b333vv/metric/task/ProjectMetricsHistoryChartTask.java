package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.ProjectHistoryChartDataCalculator;
import org.knowm.xchart.XYChart;
import org.jetbrains.annotations.NotNull;

public class ProjectMetricsHistoryChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get project metrics history chart from cache";
    private static final String STARTED_MESSAGE = "Building project metrics history chart started";
    private static final String FINISHED_MESSAGE = "Building project metrics history chart finished";
    private static final String CANCELED_MESSAGE = "Building project metrics history chart canceled";

    public ProjectMetricsHistoryChartTask(Project project) {
        super(project, "Building Project Metrics History Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        XYChart projectMetricsHistoryChart = myProject.getService(CacheService.class)
                .getUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART);
        if (projectMetricsHistoryChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProjectHistoryChartDataCalculator calculator = new ProjectHistoryChartDataCalculator();
            projectMetricsHistoryChart = calculator.calculate(myProject);
            myProject.getService(CacheService.class).putUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART, projectMetricsHistoryChart);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsHistoryXyChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
