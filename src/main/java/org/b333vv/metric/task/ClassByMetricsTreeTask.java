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

import org.b333vv.metric.event.MetricsEventListener;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.service.ClassMetricsTreeService;
import org.b333vv.metric.builder.MetricsBackgroundableTask;

public class ClassByMetricsTreeTask extends MetricsBackgroundableTask<Void> {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles classes distribution by metric values tree from cache";
    private static final String STARTED_MESSAGE = "Building classes distribution by metric values tree started";
    private static final String FINISHED_MESSAGE = "Building classes distribution by metric values tree finished";
    private static final String CANCELED_MESSAGE = "Building classes distribution by metric values tree canceled";

    public ClassByMetricsTreeTask(Project project) {
        super(project, "Building Class Distribution by Metric Values", true, (indicator) -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
            project.getService(ClassMetricsTreeService.class).getSortedClassesTreeModel(indicator);
            return null;
        }, (res) -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classByMetricTreeIsReady(null);
        }, () -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
        }, null);
    }
}
