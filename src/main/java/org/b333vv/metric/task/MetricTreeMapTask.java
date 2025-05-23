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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.treemap.builder.MetricTypeColorProvider;
import org.b333vv.metric.ui.treemap.builder.TreeMapBuilder;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.jetbrains.annotations.NotNull;


public class MetricTreeMapTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles treemap with metric types distribution from cache";
    private static final String STARTED_MESSAGE = "Building treemap with metric types distribution started";
    private static final String FINISHED_MESSAGE = "Building treemap with metric types distribution finished";
    private static final String CANCELED_MESSAGE = "Building treemap with metric types distribution canceled";

    public MetricTreeMapTask(Project project) {
        super(project, "Build Metric Treemap");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        MetricTreeMap<JavaCode> metricTreeMap = myProject.getService(MetricTaskCache.class).getUserData(MetricTaskCache.METRIC_TREE_MAP);
        if (metricTreeMap == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            AnalysisScope scope = new AnalysisScope(myProject);
            scope.setIncludeTestSource(false);
            JavaProject javaProject = myProject.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
            TreeMapBuilder treeMapBuilder = new TreeMapBuilder(javaProject);
            metricTreeMap = treeMapBuilder.getTreeMap();
            metricTreeMap.setColorProvider(new MetricTypeColorProvider(MetricType.NCSS, myProject));
            metricTreeMap.setSelectionChangedAction((String text) ->
                    myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                            .setProjectPanelBottomText(text));
            metricTreeMap.setClickedAction((JavaClass javaClass) ->
                    myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                            .projectTreeMapCellClicked(javaClass));
            myProject.getService(MetricTaskCache.class).putUserData(MetricTaskCache.METRIC_TREE_MAP, metricTreeMap);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                .metricTreeMapIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
