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

package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class MetricsIcons {

  public static final Icon PROJECT_METRIC = IconLoader.getIcon("/images/projectMetric.svg");
  public static final Icon PACKAGE_METRIC = IconLoader.getIcon("/images/packageMetric.svg");
  public static final Icon CLASS_METRIC = IconLoader.getIcon("/images/classMetric.svg");
  public static final Icon METHOD_METRIC = IconLoader.getIcon("/images/methodMetric.svg");
  public static final Icon CONSTRUCTOR = IconLoader.getIcon("/images/constructor.svg");
  public static final Icon NA = IconLoader.getIcon("/images/na.svg");
  public static final Icon INCREASED = IconLoader.getIcon("/images/increased.svg");
  public static final Icon DECREASED = IconLoader.getIcon("/images/decreased.svg");
  public static final Icon EQUAL = IconLoader.getIcon("/images/equal.svg");
  public static final Icon NOT_TRACKED = IconLoader.getIcon("/images/notTracked.svg");
  public static final Icon INVALID_VALUE = IconLoader.getIcon("/images/invalidValue.svg");
  public static final Icon VALID_VALUE = IconLoader.getIcon("/images/validValue.svg");
  public static final Icon GIT = IconLoader.getIcon("/images/git.svg");

  private MetricsIcons() {
    // only static
  }
}
