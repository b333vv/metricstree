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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.builder.ProjectMetricXYChartDataBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartBuilder;
import org.b333vv.metric.service.CacheService;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.XYChart;

import java.util.Map;
import java.util.TreeMap;

public class XyChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles package level metrics distribution chart from cache";
    private static final String STARTED_MESSAGE = "Building package level metrics distribution chart started";
    private static final String FINISHED_MESSAGE = "Building package level metrics distribution chart finished";
    private static final String CANCELED_MESSAGE = "Building package level metrics distribution chart canceled";

    public XyChartTask(Project project) {
        super(project, "Building XY Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        Map<String, Double> instability = myProject.getService(CacheService.class).getUserData(CacheService.INSTABILITY);
        Map<String, Double> abstractness = myProject.getService(CacheService.class).getUserData(CacheService.ABSTRACTNESS);
        XYChart xyChart = myProject.getService(CacheService.class).getUserData(CacheService.XY_CHART);
        if (instability == null || abstractness == null || xyChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            JavaProject javaProject = myProject.getService(MetricTaskManager.class).getPackageOnlyModel(indicator);
            instability = new TreeMap<>();
            abstractness = new TreeMap<>();
            ProjectMetricXYChartDataBuilder.build(javaProject, instability, abstractness);
            ProjectMetricXYChartBuilder builder = new ProjectMetricXYChartBuilder(myProject);
            xyChart = builder.createChart(instability, abstractness);
            myProject.getService(CacheService.class).putUserData(CacheService.INSTABILITY, instability);
            myProject.getService(CacheService.class).putUserData(CacheService.ABSTRACTNESS, abstractness);
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
