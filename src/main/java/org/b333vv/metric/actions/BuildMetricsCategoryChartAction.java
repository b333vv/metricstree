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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.task.CategoryChartTask;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.NotNull;

public class BuildMetricsCategoryChartAction extends AbstractAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
        Project project = e.getProject();
        if (project != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearProjectPanel();
            CategoryChartTask categoryChartTask = new CategoryChartTask(project);
            project.getService(TaskQueueService.class).queue(categoryChartTask);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            BasicMetricsValidRangesSettings basicMetricsValidRangesSettings = project.getService(SettingsService.class)
                    .getBasicMetricsSettings();
            e.getPresentation().setEnabled(
                    project.getService(SettingsService.class).isControlValidRanges()
                            && basicMetricsValidRangesSettings.getControlledMetricsList().stream()
                            .anyMatch(s -> s.getLevel().equals("Class Level")) &&
                            project.getService(TaskQueueService.class).isQueueEmpty());
        }
    }
}
