package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.ProfileRadarDataCalculator;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProfileRadarChartsTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get profile radar charts from cache";
    private static final String STARTED_MESSAGE = "Building profile radar charts started";
    private static final String FINISHED_MESSAGE = "Building profile radar charts finished";
    private static final String CANCELED_MESSAGE = "Building profile radar charts canceled";

    public ProfileRadarChartsTask(Project project) {
        super(project, "Building Profile Radar Charts");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts = myProject.getService(CacheService.class)
                .getUserData(CacheService.RADAR_CHART);
        if (radarCharts == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProfileRadarDataCalculator calculator = new ProfileRadarDataCalculator();
            radarCharts = calculator.calculate(myProject.getService(CacheService.class).getClassesByProfile(), myProject);
            myProject.getService(CacheService.class).putUserData(CacheService.RADAR_CHART, radarCharts);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesRadarChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
