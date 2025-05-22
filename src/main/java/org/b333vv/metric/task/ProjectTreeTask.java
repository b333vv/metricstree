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
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;


public class  ProjectTreeTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles tree model from cache";
    private static final String STARTED_MESSAGE = "Building tree model started";
    private static final String FINISHED_MESSAGE = "Building tree model finished";
    private static final String CANCELED_MESSAGE = "Building tree model canceled";

    public ProjectTreeTask(Project project) {
        super(project, "Build Project Tree");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        DefaultTreeModel metricsTreeModel = myProject.getService(MetricTaskCache.class).getUserData(MetricTaskCache.PROJECT_TREE);
        ProjectMetricTreeBuilder projectMetricTreeBuilder = myProject.getService(MetricTaskCache.class).getUserData(MetricTaskCache.TREE_BUILDER);
        if (metricsTreeModel == null || projectMetricTreeBuilder == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
//            AnalysisScope scope = new AnalysisScope(MetricsUtils.getCurrentProject());
            AnalysisScope scope = new AnalysisScope(myProject);
            scope.setIncludeTestSource(false);
            JavaProject javaProject = getProjectModel(indicator);
            projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject, myProject);
            metricsTreeModel = projectMetricTreeBuilder.createMetricTreeModel();
            myProject.getService(MetricTaskCache.class).putUserData(MetricTaskCache.PROJECT_TREE, metricsTreeModel);
            myProject.getService(MetricTaskCache.class).putUserData(MetricTaskCache.TREE_BUILDER, projectMetricTreeBuilder);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                .projectMetricsTreeIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
