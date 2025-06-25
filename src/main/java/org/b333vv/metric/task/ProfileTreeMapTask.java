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
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.treemap.builder.ProfileColorProvider;
import org.b333vv.metric.ui.treemap.builder.TreeMapBuilder;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ProfileTreeMapTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles treemap with metric profiles distribution from cache";
    private static final String STARTED_MESSAGE = "Building treemap with metric profiles distribution started";
    private static final String FINISHED_MESSAGE = "Building treemap with metric profiles distribution finished";
    private static final String CANCELED_MESSAGE = "Building treemap with metric profiles distribution canceled";

    public ProfileTreeMapTask(Project project) {
        super(project, "Build Profile Treemap");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getService(MetricTaskManager.class).getMetricProfilesDistribution(indicator);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        MetricTreeMap<JavaCode> metricTreeMap = myProject.getService(CacheService.class).getUserData(CacheService.PROFILE_TREE_MAP);
        if (metricTreeMap == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            AnalysisScope scope = new AnalysisScope(myProject);
            scope.setIncludeTestSource(false);
            JavaProject javaProject = myProject.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
            TreeMapBuilder treeMapBuilder = new TreeMapBuilder(javaProject);
            metricTreeMap = treeMapBuilder.getTreeMap();
            metricTreeMap.setColorProvider(new ProfileColorProvider(Set.of()));
            metricTreeMap.setSelectionChangedAction((String text) ->
                    myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                            .setProfilePanelBottomText(text));
            metricTreeMap.setClickedAction((JavaClass javaClass) ->
                    myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                            .profileTreeMapCellClicked(javaClass));
            myProject.getService(CacheService.class).putUserData(CacheService.PROFILE_TREE_MAP, metricTreeMap);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                .profileTreeMapIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
