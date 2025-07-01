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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.builder.ProjectHistoryChartDataCalculator;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.XYChart;

public class ProjectMetricsHistoryXyChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get project metrics history chart from cache";
    private static final String STARTED_MESSAGE = "Building project metrics history chart started";
    private static final String FINISHED_MESSAGE = "Building project metrics history chart finished";
    private static final String CANCELED_MESSAGE = "Building project metrics history chart canceled";

    public ProjectMetricsHistoryXyChartTask(Project project) {
        super(project, "Building Project Metrics History Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        XYChart xyChart = myProject.getService(CacheService.class).getUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART);
        if (xyChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            ProjectHistoryChartDataCalculator calculator = new ProjectHistoryChartDataCalculator();
            xyChart = calculator.calculate(myProject);
            myProject.getService(CacheService.class).putUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART, xyChart);
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
