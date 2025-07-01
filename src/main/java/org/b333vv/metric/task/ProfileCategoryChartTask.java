package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.ProfileCategoryChartDataCalculator;
import org.knowm.xchart.CategoryChart;
import org.jetbrains.annotations.NotNull;

public class ProfileCategoryChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get profile category chart from cache";
    private static final String STARTED_MESSAGE = "Building profile category chart started";
    private static final String FINISHED_MESSAGE = "Building profile category chart finished";
    private static final String CANCELED_MESSAGE = "Building profile category chart canceled";

    public ProfileCategoryChartTask(Project project) {
        super(project, "Building Profile Category Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        CategoryChart categoryChart = myProject.getService(CacheService.class)
                .getUserData(CacheService.PROFILE_CATEGORY_CHART);
        if (categoryChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProfileCategoryChartDataCalculator calculator = new ProfileCategoryChartDataCalculator();
            categoryChart = calculator.calculate(myProject.getService(CacheService.class).getClassesByProfile());
            myProject.getService(CacheService.class).putUserData(CacheService.PROFILE_CATEGORY_CHART, categoryChart);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesCategoryChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
