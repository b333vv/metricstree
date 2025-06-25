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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.CacheService;
import org.jetbrains.annotations.NotNull;

public class InvalidateCachesTask extends Task.Backgroundable {
    private static final String STARTED_MESSAGE = "Invalidating caches started";
    private static final String FINISHED_MESSAGE = "Invalidating caches finished";
    private final VirtualFile virtualFile;

    public InvalidateCachesTask(Project project, VirtualFile file) {
        super(project, "Invalidate Caches");
        virtualFile = file;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
        CacheService cacheService = myProject.getService(CacheService.class);
        cacheService.invalidateUserData();
        cacheService.removeJavaFile(virtualFile);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
    }
}
