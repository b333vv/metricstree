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

package org.b333vv.metric.util;

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
