package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.b333vv.metric.ui.settings.MetricsConfigurable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigureProjectAction extends AbstractAction {

  @Override
  protected boolean isEnabled(AnActionEvent e) {
    return true;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    MetricsConfigurable metricsConfigurable = new MetricsConfigurable(Objects.requireNonNull(e.getProject()));
    ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), metricsConfigurable);
  }
}
