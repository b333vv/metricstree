package org.b333vv.metric.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.ProfileTreeMapModelCalculator;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.jetbrains.annotations.NotNull;

public class ProfileTreeMapTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get profile tree map from cache";
    private static final String STARTED_MESSAGE = "Building profile tree map started";
    private static final String FINISHED_MESSAGE = "Building profile tree map finished";
    private static final String CANCELED_MESSAGE = "Building profile tree map canceled";

    public ProfileTreeMapTask(Project project) {
        super(project, "Building Profile Tree Map");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        MetricTreeMap<JavaCode> profileTreeMap = myProject.getService(CacheService.class)
                .getUserData(CacheService.PROFILE_TREE_MAP);
        if (profileTreeMap == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProfileTreeMapModelCalculator calculator = new ProfileTreeMapModelCalculator();
            profileTreeMap = calculator.calculate(myProject.getService(CacheService.class).getProject());
            myProject.getService(CacheService.class).putUserData(CacheService.PROFILE_TREE_MAP, profileTreeMap);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profileTreeMapIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}