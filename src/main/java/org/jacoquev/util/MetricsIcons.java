package org.jacoquev.util;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class MetricsIcons {

  public static final Icon PROJECT_METRIC = IconLoader.getIcon("/images/projectMetric11Yellow.svg");
  public static final Icon PACKAGE_METRIC = IconLoader.getIcon("/images/packageMetric11Yellow.svg");
  public static final Icon CLASS_METRIC = IconLoader.getIcon("/images/classMetric11Yellow.svg");
  public static final Icon METHOD_METRIC = IconLoader.getIcon("/images/methodMetric11Yellow.svg");
  public static final Icon CONSTRUCTOR = IconLoader.getIcon("/images/constructor.svg");
  public static final Icon NA = IconLoader.getIcon("/images/na.svg");

  private MetricsIcons() {
    // only static
  }
}
