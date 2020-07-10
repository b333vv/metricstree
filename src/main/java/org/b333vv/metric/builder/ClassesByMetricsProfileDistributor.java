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

package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.DerivativeMetricsRange;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.settings.profile.MetricProfileItem;
import org.b333vv.metric.ui.settings.profile.MetricProfileSettings;
import org.b333vv.metric.util.MetricsUtils;

import java.util.*;

public class ClassesByMetricsProfileDistributor {
    public static Map<MetricProfile, Set<JavaClass>> classesByMetricsProfileDistribution(JavaProject javaProject) {
        Map<MetricProfile, Set<JavaClass>> profileSetMap = new TreeMap<>();
        for (MetricProfile profile : metricProfiles()) {
            Set<JavaClass> classes = new HashSet<>();
            javaProject.allClasses()
                    .forEach(c -> {
                        if (checkClass(c, profile)) {
                            classes.add(c);
                        }
                    });
            profileSetMap.put(profile, classes);
        }
        return profileSetMap;
    }

    private static boolean checkClass(JavaClass javaClass, MetricProfile profile) {
        for (Map.Entry<MetricType, Range> entry : profile.getProfile().entrySet()) {
            if (entry.getKey().level() == MetricLevel.CLASS) {
                Metric m = javaClass.metric(entry.getKey());
                if (m != null && entry.getValue().getRangeType(m.getValue()) != RangeType.REGULAR) {
                    return false;
                }
            } else if (entry.getKey().level() == MetricLevel.METHOD) {
                Optional<Boolean> checkMethod = javaClass
                        .methods()
                        .map(javaMethod -> checkMethod(javaMethod, entry))
                        .filter(e -> e.equals(Boolean.TRUE))
                        .findAny();
                if (checkMethod.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkMethod(JavaMethod javaMethod, Map.Entry<MetricType, Range> entry) {
        Metric m = javaMethod.metric(entry.getKey());
        if (m != null && entry.getValue().getRangeType(m.getValue()) != RangeType.REGULAR) {
            return false;
        }
        return true;
    }


    private static Set<MetricProfile> metricProfiles() {
        MetricProfileSettings metricProfileSettings = MetricsUtils.get(MetricsUtils.getCurrentProject(),
                MetricProfileSettings.class);
        Map<String, List<MetricProfileItem>> savedProfiles = metricProfileSettings.getProfiles();
        Set<MetricProfile> profiles = new HashSet<>();
        for (Map.Entry<String, List<MetricProfileItem>> entry : savedProfiles.entrySet()) {
            Map<MetricType, Range> profileMap = new HashMap<>();
            for (MetricProfileItem item : entry.getValue()) {
                if (item.isLong()) {
                    profileMap.put(MetricType.valueOf(item.getName()),
                            DerivativeMetricsRange.of(Value.of(item.getMinLongValue()), Value.of(item.getMaxLongValue())));
                } else {
                    profileMap.put(MetricType.valueOf(item.getName()),
                            DerivativeMetricsRange.of(Value.of(item.getMinDoubleValue()), Value.of(item.getMaxDoubleValue())));
                }
            }
            MetricProfile profile = new MetricProfile(entry.getKey(), profileMap);
            profiles.add(profile);
        }
        return profiles;
    }
}
