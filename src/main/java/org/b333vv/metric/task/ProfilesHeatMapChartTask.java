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
import org.b333vv.metric.builder.ProfileHeatMapDataCalculator;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.HeatMapChart;

import java.util.Map;
import java.util.Set;

public class ProfilesHeatMapChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get metric profiles correlation chart from cache";
    private static final String STARTED_MESSAGE = "Building metric profiles correlation chart started";
    private static final String FINISHED_MESSAGE = "Building metric profiles correlation chart finished";
    private static final String CANCELED_MESSAGE = "Building metric profiles correlation chart canceled";

    public ProfilesHeatMapChartTask(Project project) {
        super(project, "Build Metric Profiles Correlation Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        HeatMapChart heatMapChart = myProject.getService(CacheService.class).getUserData(CacheService.HEAT_MAP_CHART);
        if (heatMapChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            Map<FitnessFunction, Set<ClassElement>> classesByProfile = myProject.getService(CacheService.class).getClassesByProfile();
            ProfileHeatMapDataCalculator calculator = new ProfileHeatMapDataCalculator();
            heatMapChart = calculator.calculate(classesByProfile);
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
