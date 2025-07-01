package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.ProfileBoxChartDataCalculator;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProfileBoxChartsTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get profile box charts from cache";
    private static final String STARTED_MESSAGE = "Building profile box charts started";
    private static final String FINISHED_MESSAGE = "Building profile box charts finished";
    private static final String CANCELED_MESSAGE = "Building profile box charts canceled";

    public ProfileBoxChartsTask(Project project) {
        super(project, "Building Profile Box Charts");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        List<ProfileBoxChartBuilder.BoxChartStructure> boxCharts = myProject.getService(CacheService.class)
                .getUserData(CacheService.BOX_CHARTS);
        if (boxCharts == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProfileBoxChartDataCalculator calculator = new ProfileBoxChartDataCalculator();
            boxCharts = calculator.calculate(myProject.getService(CacheService.class).getClassesByProfile());
            myProject.getService(CacheService.class).putUserData(CacheService.BOX_CHARTS, boxCharts);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesBoxChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
