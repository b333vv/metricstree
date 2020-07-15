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
    Icon PROJECT_METRIC = IconLoader.getIcon("/icons/projectMetric.svg");
    Icon PACKAGE_METRIC = IconLoader.getIcon("/icons/packageMetric.svg");
    Icon CLASS_METRIC = IconLoader.getIcon("/icons/classMetric.svg");
    Icon METHOD_METRIC = IconLoader.getIcon("/icons/methodMetric.svg");
    Icon CONSTRUCTOR = IconLoader.getIcon("/icons/constructor.svg");
    Icon NA = IconLoader.getIcon("/icons/na.svg");
    Icon INCREASED = IconLoader.getIcon("/icons/increased.svg");
    Icon DECREASED = IconLoader.getIcon("/icons/decreased.svg");
    Icon EQUAL = IconLoader.getIcon("/icons/equal.svg");
    Icon NOT_TRACKED = IconLoader.getIcon("/icons/notTracked.svg");
    Icon SORT_BY_VALUES = IconLoader.getIcon("/icons/sortByValues.svg");
    Icon BAR_CHART = IconLoader.getIcon("/icons/chartBar.svg");
    Icon PIE_CHART = IconLoader.getIcon("/icons/chartPie.svg");
    Icon XY_CHART = IconLoader.getIcon("/icons/chartXY.svg");
    Icon BOX_CHART = IconLoader.getIcon("/icons/chartBox.svg");
    Icon HEAT_MAP_CHART = IconLoader.getIcon("/icons/chartHeatMap.svg");
    Icon RADAR_CHART = IconLoader.getIcon("/icons/chartRadar.svg");
    Icon REGULAR_COLOR = IconLoader.getIcon("/icons/regularColor.svg");
    Icon HIGH_COLOR = IconLoader.getIcon("/icons/highColor.svg");
    Icon VERY_HIGH_COLOR = IconLoader.getIcon("/icons/veryHighColor.svg");
    Icon EXTREME_COLOR = IconLoader.getIcon("/icons/extremeColor.svg");
    Icon CSV = IconLoader.getIcon("/icons/csv.svg");
    Icon XML = IconLoader.getIcon("/icons/xml.svg");
    Icon PROFILE_TABLE = IconLoader.getIcon("/icons/profileTable.svg");
    Icon PROJECT_TREE = IconLoader.getIcon("/icons/metricsTree_dark.svg");
    Icon TREE_MAP = IconLoader.getIcon("/icons/treeMap.svg");
}
