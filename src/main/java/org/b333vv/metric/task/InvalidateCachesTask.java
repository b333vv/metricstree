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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.builder.MetricsBackgroundableTask;

public class InvalidateCachesTask extends MetricsBackgroundableTask<Void> {
    private static final String STARTED_MESSAGE = "Invalidating caches started";
    private static final String FINISHED_MESSAGE = "Invalidating caches finished";

    public InvalidateCachesTask(Project project, VirtualFile file) {
        super(project, "Invalidate Caches", true, (indicator) -> {
            // The project can become disposed in unit-test light projects before this
            // background task is executed.
            // Guard against accessing the message bus or services of a disposed project to
            // avoid PluginException.
            if (project == null || project.isDisposed()) {
                return null;
            }
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            CacheService cacheService = project.getService(CacheService.class);
            if (cacheService != null) {
                cacheService.invalidateUserData();
                cacheService.removeJavaFile(file);
            }
            if (!project.isDisposed()) {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
            }
            return null;
        }, null, null, null);
    }
}
