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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.b333vv.metric.ui.tool.ClassMetricsPanel;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Objects;

public class FilterClassMetricsTreeAction extends AbstractAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
        showPopup(e);
    }

    @Override
    public void update (AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(MetricsUtils.isClassMetricsTreeExists());
    }

    private void showPopup(@NotNull AnActionEvent e) {
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup =
                (DefaultActionGroup) actionManager.getAction("Metrics.FilterClassMetricsTreeGroup");

        ActionPopupMenu actionPopupMenu =
                ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, actionGroup);

        ToolWindow toolWindow =
                ToolWindowManager.getInstance(Objects.requireNonNull(e.getProject()))
                        .getToolWindow("MetricsTree");
        actionPopupMenu.getComponent().show(
                toolWindow.getComponent().getComponent(0),
                ((MouseEvent) e.getInputEvent()).getX(),
                ((MouseEvent) e.getInputEvent()).getY());
    }
}
