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

package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.task.ExportClassMetricsToCsvTask;
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.task.MetricTaskManager;
import org.jetbrains.annotations.NotNull;

public class ExportClassMetricsToCsvAction extends AbstractAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
        Project project = e.getProject();
        if (project != null) {
            String fileName = MetricTaskManager.getFileName("csv", project);
            if (fileName != null && !fileName.isBlank()) {
                ExportClassMetricsToCsvTask exportToCsvTask = new ExportClassMetricsToCsvTask(project, fileName);
                MetricTaskCache.runTask(project, exportToCsvTask);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null && MetricTaskCache.isQueueEmpty(project));
    }
}
