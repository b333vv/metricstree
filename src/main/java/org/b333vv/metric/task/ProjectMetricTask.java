/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.task;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.builder.MoodMetricsSetCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import static org.b333vv.metric.task.MetricTaskManager.getPackageModel;

public class ProjectMetricTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get project level metrics from cache";
    private static final String STARTED_MESSAGE = "Building project level metrics";
    private static final String FINISHED_MESSAGE = "Building project level metrics finished";
    private static final String CANCELED_MESSAGE = "Building project level metrics canceled";

    public ProjectMetricTask() {
        super(MetricsUtils.getCurrentProject(), "Calculating Project Level Metrics");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        JavaProject javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PROJECT_METRICS);
        if (javaProject == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            javaProject = getPackageModel(indicator);
            AnalysisScope scope = new AnalysisScope(MetricsUtils.getCurrentProject());
            scope.setIncludeTestSource(false);
            MoodMetricsSetCalculator moodMetricsSetCalculator = new MoodMetricsSetCalculator(scope,
                    MetricTaskCache.instance().getUserData(MetricTaskCache.DEPENDENCIES), javaProject);
            moodMetricsSetCalculator.calculate();
            MetricTaskCache.instance().putUserData(MetricTaskCache.PROJECT_METRICS, javaProject);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
