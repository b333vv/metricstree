package org.b333vv.metricsTree.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.b333vv.metricsTree.ui.settings.MetricsConfigurable;
import org.jetbrains.annotations.NotNull;

public class ConfigureProjectAction extends AbstractAction {

  @Override
  protected boolean isEnabled(AnActionEvent e) {
    return true;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    MetricsConfigurable metricsConfigurable = new MetricsConfigurable(e.getProject());
    ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), metricsConfigurable);
  }
}
