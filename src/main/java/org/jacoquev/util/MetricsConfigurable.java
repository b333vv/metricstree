package org.jacoquev.util;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jacoquev.ui.settings.SettingsPanel;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nullable;
import javax.swing.*;

/**
 * Coordinates creation of models and visual components from persisted settings.
 * Transforms objects as needed and keeps track of changes.
 */
public class MetricsConfigurable implements Configurable, Configurable.NoMargin, Configurable.NoScroll {
  private final Project project;
  private final MetricsAllowableValueRanges metricsAllowableValueRanges;
  private final ClassMetricsTreeSettings classMetricsTreeSettings;

  private SettingsPanel panel;

  public MetricsConfigurable(Project project) {
    this.project = project;
    this.metricsAllowableValueRanges = project.getComponent(MetricsAllowableValueRanges.class);
    this.classMetricsTreeSettings = project.getComponent(ClassMetricsTreeSettings.class);
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Jacoquev";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    if (panel == null) {
      panel = new SettingsPanel(project);
    }
    return panel.getRootPane();
  }

  @Override
  public boolean isModified() {
    return panel != null && (panel.isModified(metricsAllowableValueRanges)
                                    || panel.isModified(classMetricsTreeSettings));
  }

  @Override
  public void apply() {
    if (panel != null) {
      panel.save(metricsAllowableValueRanges);
      panel.save(classMetricsTreeSettings);
      onSave();
    }
  }

  private void onSave() {

  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {

  }
}
