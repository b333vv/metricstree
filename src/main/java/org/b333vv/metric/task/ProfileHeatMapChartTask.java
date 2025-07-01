package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.ProfileHeatMapDataCalculator;
import org.knowm.xchart.HeatMapChart;
import org.jetbrains.annotations.NotNull;

public class ProfileHeatMapChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get profile heat map chart from cache";
    private static final String STARTED_MESSAGE = "Building profile heat map chart started";
    private static final String FINISHED_MESSAGE = "Building profile heat map chart finished";
    private static final String CANCELED_MESSAGE = "Building profile heat map chart canceled";

    public ProfileHeatMapChartTask(Project project) {
        super(project, "Building Profile Heat Map Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        HeatMapChart heatMapChart = myProject.getService(CacheService.class)
                .getUserData(CacheService.HEAT_MAP_CHART);
        if (heatMapChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProfileHeatMapDataCalculator calculator = new ProfileHeatMapDataCalculator();
            heatMapChart = calculator.calculate(myProject.getService(CacheService.class).getClassesByProfile());
            myProject.getService(CacheService.class).putUserData(CacheService.HEAT_MAP_CHART, heatMapChart);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesHeatMapChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
