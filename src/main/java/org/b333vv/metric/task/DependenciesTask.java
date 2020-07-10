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
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.builder.DependenciesCalculator;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class DependenciesTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to get dependencies map from cache";
    private static final String STARTED_MESSAGE = "Building dependencies map";
    private static final String FINISHED_MESSAGE = "Building dependencies map finished";
    private static final String CANCELED_MESSAGE = "Building dependencies map canceled";

    public DependenciesTask() {
        super(MetricsUtils.getCurrentProject(), "Building Dependencies");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        DependenciesBuilder dependenciesBuilder = MetricTaskCache.instance().getUserData(MetricTaskCache.DEPENDENCIES);
        if (dependenciesBuilder == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            AnalysisScope scope = new AnalysisScope(MetricsUtils.getCurrentProject());
            scope.setIncludeTestSource(false);
            dependenciesBuilder = new DependenciesBuilder();
            DependenciesCalculator dependenciesCalculator = new DependenciesCalculator(scope, dependenciesBuilder);
            dependenciesCalculator.calculateDependencies();
            MetricTaskCache.instance().putUserData(MetricTaskCache.DEPENDENCIES, dependenciesBuilder);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
