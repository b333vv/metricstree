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
import org.b333vv.metric.builder.ClassesByMetricsValuesCounter;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.chart.builder.MetricCategoryChartBuilder;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.CategoryChart;

import java.util.Map;

public class CategoryChartTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles classes distribution by metric values category chart from cache";
    private static final String STARTED_MESSAGE = "Building classes distribution by metric values category chart started";
    private static final String FINISHED_MESSAGE = "Building classes distribution by metric values category chart finished";
    private static final String CANCELED_MESSAGE = "Building classes distribution by metric values category chart canceled";

    public CategoryChartTask(Project project) {
        super(project, "Building Category Chart");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        Map<MetricType, Map<RangeType, Double>> classesByMetricTypes = myProject.getService(MetricTaskCache.class)
                .getUserData(MetricTaskCache.CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART);
        CategoryChart categoryChart = myProject.getService(MetricTaskCache.class)
                .getUserData(MetricTaskCache.CATEGORY_CHART);
        if (classesByMetricTypes == null || categoryChart == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            JavaProject javaProject = myProject.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
            ClassesByMetricsValuesCounter classesByMetricsValuesCounter = new ClassesByMetricsValuesCounter();
            classesByMetricTypes = classesByMetricsValuesCounter.classesByMetricsValuesDistribution(javaProject);
            MetricCategoryChartBuilder builder = new MetricCategoryChartBuilder();
            categoryChart = builder.createChart(classesByMetricTypes);
            myProject.getService(MetricTaskCache.class).putUserData(MetricTaskCache.CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART, classesByMetricTypes);
            myProject.getService(MetricTaskCache.class).putUserData(MetricTaskCache.CATEGORY_CHART, categoryChart);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).categoryChartIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
