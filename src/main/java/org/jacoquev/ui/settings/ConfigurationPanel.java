package org.jacoquev.ui.settings;

import javax.swing.*;

public interface ConfigurationPanel<T> {

  JComponent getComponent();

  boolean isModified(T settings);

  void save(T settings);

  void load(T settings);
}
