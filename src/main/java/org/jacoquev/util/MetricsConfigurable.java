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
  private final MetricsSettings metricsSettings;

  private SettingsPanel panel;

  public MetricsConfigurable(Project project) {
    this.project = project;
    this.metricsSettings = project.getComponent(MetricsSettings.class);
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Come4J";
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
    return panel != null && panel.isModified(metricsSettings);
  }

  @Override
  public void apply() {
    if (panel != null) {
      panel.save(metricsSettings);
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
