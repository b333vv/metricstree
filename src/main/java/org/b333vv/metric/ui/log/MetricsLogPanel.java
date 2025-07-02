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

package org.b333vv.metric.ui.log;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import javax.swing.*;

public class MetricsLogPanel extends SimpleToolWindowPanel {
  private static final String ID = "MetricsTree";

  private final Project project;

  public MetricsLogPanel(Project project) {
    super(false, true);
    this.project = project;

    addConsole();
    addToolbar();
  }

  private void addConsole() {

    ConsoleView consoleView = project.getService(MetricsConsole.class).getConsoleView();
//    ConsoleView consoleView = project.getComponent(MetricsConsole.class).getConsoleView();
    super.setContent(consoleView.getComponent());
  }

  private void addToolbar() {
    final ActionManager actionManager = ActionManager.getInstance();
    DefaultActionGroup actionGroup =
            (DefaultActionGroup) actionManager.getAction("Metrics.CleanConsole");
    ActionToolbar mainToolbar = ActionManager.getInstance().createActionToolbar(ID, actionGroup, false);
    mainToolbar.setTargetComponent(this);
    Box toolBarBox = Box.createHorizontalBox();
    toolBarBox.add(mainToolbar.getComponent());

    super.setToolbar(toolBarBox);
    mainToolbar.getComponent().setVisible(true);
  }
}
