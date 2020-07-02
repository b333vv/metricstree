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
  public static final Icon REGULAR_VALUE = IconLoader.getIcon("/images/regularValue.svg");
  public static final Icon HIGH_VALUE = IconLoader.getIcon("/images/highValue.svg");
  public static final Icon VERY_HIGH_VALUE = IconLoader.getIcon("/images/veryHighValue.svg");
  public static final Icon EXTREME_VALUE = IconLoader.getIcon("/images/extremeValue.svg");
  public static final Icon SORT_BY_VALUES = IconLoader.getIcon("/images/sortByValues.svg");
  public static final Icon CALCULATE_PROJECT_METRICS = IconLoader.getIcon("/images/calculateProjectMetrics.svg");
  public static final Icon BAR_CHART = IconLoader.getIcon("/images/chartBar.svg");
  public static final Icon PIE_CHART = IconLoader.getIcon("/images/chartPie.svg");
  public static final Icon XY_CHART = IconLoader.getIcon("/images/chartXY.svg");
  public static final Icon REGULAR_COLOR = IconLoader.getIcon("/images/regularColor.svg");
  public static final Icon HIGH_COLOR = IconLoader.getIcon("/images/highColor.svg");
  public static final Icon VERY_HIGH_COLOR = IconLoader.getIcon("/images/veryHighColor.svg");
  public static final Icon EXTREME_COLOR = IconLoader.getIcon("/images/extremeColor.svg");

  private MetricsIcons() {
    // only static
  }
}
