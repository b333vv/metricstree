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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.b333vv.metric.task.MetricTaskManager.getMetricProfilesDistribution;

public class ProfilesRadarChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles invalid metrics values and metric profiles correlation chart from cache";
    private static final String STARTED_MESSAGE = "Building invalid metrics values and metric profiles correlation chart started";
    private static final String FINISHED_MESSAGE = "Building invalid metrics values and metric profiles correlation chart  finished";
    private static final String CANCELED_MESSAGE = "Building invalid metrics values and metric profiles correlation chart  canceled";

    public ProfilesRadarChartTask() {
        super(MetricsUtils.getCurrentProject(), "Build Invalid Metrics Values And Metric Profiles Correlation Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts = MetricTaskCache.instance().getUserData(MetricTaskCache.RADAR_CHART);
        if (radarCharts == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            Map<FitnessFunction, Set<JavaClass>> classesByMetricProfile = getMetricProfilesDistribution(indicator);
            ProfileRadarChartBuilder profileRadarChartBuilder = new ProfileRadarChartBuilder();
            radarCharts = profileRadarChartBuilder.createChart(classesByMetricProfile, myProject);
            MetricTaskCache.instance().putUserData(MetricTaskCache.RADAR_CHART, radarCharts);
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
