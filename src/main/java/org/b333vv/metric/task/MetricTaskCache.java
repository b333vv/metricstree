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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.b333vv.metric.service.TaskQueueService;

import javax.swing.tree.DefaultTreeModel;

/**
 * @deprecated This class is deprecated and will be removed in a future release.
 * Use {@link org.b333vv.metric.service.CacheService} instead.
 */
@Deprecated
@Service(Service.Level.PROJECT)
public final class MetricTaskCache implements Disposable {
    private final Project project;

    public MetricTaskCache(Project project) {
        this.project = project;
    }

    @Override
    public void dispose() {
    }

    private void invalidateCaches(VirtualFile file) {
        InvalidateCachesTask invalidateCachesTask = new InvalidateCachesTask(this.project, file);
        project.getService(TaskQueueService.class).queue(invalidateCachesTask);
    }
}
