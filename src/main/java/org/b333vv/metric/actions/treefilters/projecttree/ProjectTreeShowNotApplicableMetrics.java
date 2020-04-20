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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowNotApplicableMetrics extends ToggleAction {


    public ProjectTreeShowNotApplicableMetrics() {
        super("Show Not Applicable Metrics", "Show or dont show not applicable metrics",
                AllIcons.General.BalloonWarning);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isNotApplicableMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setNotApplicableMetricsVisible(state);
        Project project = e.getProject();
        if (project != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).buildProjectMetricsTree();
        }
    }
}
