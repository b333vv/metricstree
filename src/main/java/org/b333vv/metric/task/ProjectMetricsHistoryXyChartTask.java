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
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.builder.ProjectMetricsSet2Json;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.ui.chart.builder.ProjectMetricsHistoryXYChartBuilder;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.knowm.xchart.XYChart;

import java.util.*;

public class ProjectMetricsHistoryXyChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get project metrics history chart from cache";
    private static final String STARTED_MESSAGE = "Building project metrics history chart started";
    private static final String FINISHED_MESSAGE = "Building project metrics history chart finished";
    private static final String CANCELED_MESSAGE = "Building project metrics history chart canceled";

    public ProjectMetricsHistoryXyChartTask() {
        super(MetricsUtils.getCurrentProject(), "Building Project Metrics History Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        XYChart xyChart = MetricTaskCache.instance().getUserData(MetricTaskCache.PROJECT_METRICS_HISTORY_XY_CHART);
        if (xyChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            TreeSet<JSONObject> metricsStampSet = ProjectMetricsSet2Json.parseStoredMetricsSnapshots();
            if (metricsStampSet == null) {
                indicator.cancel();
                return;
            }
            ProjectMetricsHistoryXYChartBuilder builder = new ProjectMetricsHistoryXYChartBuilder();
            xyChart = builder.createChart(metricsStampSet);
            MetricTaskCache.instance().putUserData(MetricTaskCache.PROJECT_METRICS_HISTORY_XY_CHART, xyChart);
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
