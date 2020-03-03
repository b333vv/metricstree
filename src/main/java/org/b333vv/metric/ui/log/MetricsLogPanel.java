/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2019 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.b333vv.metric.ui.log;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;

public class MetricsLogPanel extends SimpleToolWindowPanel {
  private static final String ID = "MetricsTree";

  private final ToolWindow toolWindow;
  private final Project project;

  private ActionToolbar mainToolbar;

  public MetricsLogPanel(ToolWindow toolWindow, Project project) {
    super(false, true);
    this.toolWindow = toolWindow;
    this.project = project;

    addConsole();
    addToolbar();
  }

  private void addConsole() {
    ConsoleView consoleView = MetricsUtils.get(project, MetricsConsole.class).getConsoleView();
    super.setContent(consoleView.getComponent());
  }

  private void addToolbar() {
    final ActionManager actionManager = ActionManager.getInstance();
    DefaultActionGroup actionGroup =
            (DefaultActionGroup) actionManager.getAction("Metrics.CleanConsole");
    mainToolbar = ActionManager.getInstance().createActionToolbar(ID, actionGroup, false);
    mainToolbar.setTargetComponent(this);
    Box toolBarBox = Box.createHorizontalBox();
    toolBarBox.add(mainToolbar.getComponent());

    super.setToolbar(toolBarBox);
    mainToolbar.getComponent().setVisible(true);
  }
}
