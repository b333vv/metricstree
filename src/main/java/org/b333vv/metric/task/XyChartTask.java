package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.XyChartDataCalculator;
import org.knowm.xchart.XYChart;
import org.jetbrains.annotations.NotNull;

public class XyChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get XY chart from cache";
    private static final String STARTED_MESSAGE = "Building XY chart started";
    private static final String FINISHED_MESSAGE = "Building XY chart finished";
    private static final String CANCELED_MESSAGE = "Building XY chart canceled";

    public XyChartTask(Project project) {
        super(project, "Building XY Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        XYChart xyChart = myProject.getService(CacheService.class)
                .getUserData(CacheService.XY_CHART);
        if (xyChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            JavaProject javaProject = myProject.getService(CacheService.class).getProject();
            XyChartDataCalculator calculator = new XyChartDataCalculator();
            xyChart = calculator.calculate(javaProject, myProject);
            myProject.getService(CacheService.class).putUserData(CacheService.XY_CHART, xyChart);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).xyChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}