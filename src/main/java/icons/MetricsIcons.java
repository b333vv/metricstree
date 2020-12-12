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

public interface MetricsIcons {
    Icon PROJECT_METRIC = IconLoader.getIcon("/icons/projectMetric.svg", MetricsIcons.class);
    Icon PACKAGE_METRIC = IconLoader.getIcon("/icons/packageMetric.svg", MetricsIcons.class);
    Icon CLASS_METRIC = IconLoader.getIcon("/icons/classMetric.svg", MetricsIcons.class);
    Icon METHOD_METRIC = IconLoader.getIcon("/icons/methodMetric.svg", MetricsIcons.class);
    Icon CONSTRUCTOR = IconLoader.getIcon("/icons/constructor.svg", MetricsIcons.class);
    Icon NA = IconLoader.getIcon("/icons/na.svg", MetricsIcons.class);
    Icon INCREASED = IconLoader.getIcon("/icons/increased.svg", MetricsIcons.class);
    Icon DECREASED = IconLoader.getIcon("/icons/decreased.svg", MetricsIcons.class);
    Icon EQUAL = IconLoader.getIcon("/icons/equal.svg", MetricsIcons.class);
    Icon NOT_TRACKED = IconLoader.getIcon("/icons/notTracked.svg", MetricsIcons.class);
    Icon SORT_BY_VALUES = IconLoader.getIcon("/icons/sortByValues.svg", MetricsIcons.class);
    Icon BAR_CHART = IconLoader.getIcon("/icons/chartBar.svg", MetricsIcons.class);
    Icon PIE_CHART = IconLoader.getIcon("/icons/chartPie.svg", MetricsIcons.class);
    Icon XY_CHART = IconLoader.getIcon("/icons/chartXY.svg", MetricsIcons.class);
    Icon BOX_CHART = IconLoader.getIcon("/icons/chartBox.svg", MetricsIcons.class);
    Icon HEAT_MAP_CHART = IconLoader.getIcon("/icons/chartHeatMap.svg", MetricsIcons.class);
    Icon RADAR_CHART = IconLoader.getIcon("/icons/chartRadar.svg", MetricsIcons.class);
    Icon REGULAR_COLOR = IconLoader.getIcon("/icons/regularColor.svg", MetricsIcons.class);
    Icon HIGH_COLOR = IconLoader.getIcon("/icons/highColor.svg", MetricsIcons.class);
    Icon VERY_HIGH_COLOR = IconLoader.getIcon("/icons/veryHighColor.svg", MetricsIcons.class);
    Icon EXTREME_COLOR = IconLoader.getIcon("/icons/extremeColor.svg", MetricsIcons.class);
    Icon CSV = IconLoader.getIcon("/icons/csv.svg", MetricsIcons.class);
    Icon XML = IconLoader.getIcon("/icons/xml.svg", MetricsIcons.class);
    Icon PROFILE_TABLE = IconLoader.getIcon("/icons/profileTable.svg", MetricsIcons.class);
    Icon PROJECT_TREE = IconLoader.getIcon("/icons/metricsTree_dark.svg", MetricsIcons.class);
    Icon TREE_MAP = IconLoader.getIcon("/icons/treeMap.svg", MetricsIcons.class);
}
