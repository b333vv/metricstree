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

package org.b333vv.metric.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import icons.MetricsIcons;
import org.b333vv.metric.actions.AbstractToggleAction;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.service.UIStateService;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeGroupMetricsByMetricSets extends AbstractToggleAction {


    public ProjectTreeGroupMetricsByMetricSets() {
        super("Group Metrics By Metric Sets", "Group or dont group metrics by metric sets", MetricsIcons.PROJECT_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return e.getProject().getService(UIStateService.class).getProjectMetricsTreeFilter().isMetricsGroupedByMetricSets();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        e.getProject().getService(UIStateService.class).getProjectMetricsTreeFilter().setMetricsGroupedByMetricSets(state);
        Project project = e.getProject();
        if (project != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).buildProjectMetricsTree();
        }
    }
}
