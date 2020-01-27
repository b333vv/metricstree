package org.jacoquev.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jacoquev.ui.log.MetricsConsole;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CleanConsoleAction extends AnAction {

  public CleanConsoleAction() {
  }

  public CleanConsoleAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
    super(text, description, icon);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    if (e.getProject() != null) {
      MetricsConsole.get(e.getProject()).clear();
    }
  }
}
