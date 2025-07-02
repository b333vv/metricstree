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
import org.b333vv.metric.service.UIStateService;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.NotNull;

class SetShowClassMetricsTreeAction extends AbstractToggleAction {

    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return false;
        }
        SettingsService settingsService = project.getService(SettingsService.class);
//        return Objects.requireNonNull(event.getProject()).getService(ClassMetricsTreeSettings1.class).isShowClassMetricsTree();
        return settingsService.isShowClassMetricsTree();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean showClassMetricsTree) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        SettingsService settingsService = project.getService(SettingsService.class);
        settingsService.setShowClassMetricsTree(showClassMetricsTree);
        project.getService(UIStateService.class).setClassMetricsTreeExists(showClassMetricsTree);
    }
}
