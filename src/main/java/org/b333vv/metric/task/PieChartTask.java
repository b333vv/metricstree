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
import org.b333vv.metric.builder.PieChartDataCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.task.MetricTaskManager;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PieChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles classes distribution by metric values pie chart from cache";
    private static final String STARTED_MESSAGE = "Building classes distribution by metric values pie chart started";
    private static final String FINISHED_MESSAGE = "Building classes distribution by metric values pie chart finished";
    private static final String CANCELED_MESSAGE = "Building classes distribution by metric values pie chart canceled";

    public PieChartTask(Project project) {
        super(project, "Building Pie Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        List<MetricPieChartBuilder.PieChartStructure> pieChartList = myProject.getService(CacheService.class)
                .getUserData(CacheService.PIE_CHART_LIST);
        if (pieChartList == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            JavaProject javaProject = myProject.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
            PieChartDataCalculator calculator = new PieChartDataCalculator(myProject);
            MetricPieChartBuilder builder = new MetricPieChartBuilder();
            pieChartList = builder.createChart(calculator.calculate(Objects.requireNonNull(javaProject)));
            myProject.getService(CacheService.class).putUserData(CacheService.PIE_CHART_LIST, pieChartList);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).pieChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
