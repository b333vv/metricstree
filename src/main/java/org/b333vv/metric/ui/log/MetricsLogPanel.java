package org.b333vv.metric.ui.log;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;

public class MetricsLogPanel extends SimpleToolWindowPanel {
  private static final String ID = "MetricsTree";

  private transient final Project project;

  public MetricsLogPanel(Project project) {
    super(false, true);
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
    ActionToolbar mainToolbar = ActionManager.getInstance().createActionToolbar(ID, actionGroup, false);
    mainToolbar.setTargetComponent(this);
    Box toolBarBox = Box.createHorizontalBox();
    toolBarBox.add(mainToolbar.getComponent());

    super.setToolbar(toolBarBox);
    mainToolbar.getComponent().setVisible(true);
  }
}
