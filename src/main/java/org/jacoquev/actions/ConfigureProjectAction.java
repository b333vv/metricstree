package org.jacoquev.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jacoquev.util.MetricsConfigurable;

public class ConfigureProjectAction extends AbstractAction {

  @Override
  protected boolean isEnabled(AnActionEvent e) {
    return true;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    MetricsConfigurable metricsConfigurable = new MetricsConfigurable(e.getProject());
    ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), metricsConfigurable);
  }
}
