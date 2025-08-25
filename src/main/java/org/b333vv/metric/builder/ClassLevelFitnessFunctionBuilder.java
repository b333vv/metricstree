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

import com.intellij.openapi.project.Project;
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
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.ui.settings.fitnessfunction.FitnessFunctionItem;
import org.b333vv.metric.ui.settings.fitnessfunction.ClassLevelFitnessFunctions;
import org.b333vv.metric.util.SettingsService;

import java.util.*;

public class ClassLevelFitnessFunctionBuilder {
    public static Map<FitnessFunction, Set<JavaClass>> classesByMetricsProfileDistribution(Project project, JavaProject javaProject) {
        Map<FitnessFunction, Set<JavaClass>> fitnessFunctionResult = new TreeMap<>();
        for (FitnessFunction profile : fitnessFunctionResult(project)) {
            Set<JavaClass> classes = new HashSet<>();
            javaProject.allClasses()
                    .forEach(c -> {
                        if (checkClass(c, profile)) {
                            classes.add(c);
                        }
                    });
            fitnessFunctionResult.put(profile, classes);
        }
        return Collections.unmodifiableMap(fitnessFunctionResult);
    }

    private static boolean checkClass(JavaClass javaClass, FitnessFunction profile) {
        for (Map.Entry<MetricType, Range> entry : profile.profile().entrySet()) {
            if (entry.getKey().level() == MetricLevel.CLASS) {
                Metric m = javaClass.metric(entry.getKey());
                if (m != null && entry.getValue().getRangeType(m.getPsiValue()) != RangeType.REGULAR) {
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
        return m == null || entry.getValue().getRangeType(m.getPsiValue()) == RangeType.REGULAR;
    }


    private static Set<FitnessFunction> fitnessFunctionResult(Project project) {
        ClassLevelFitnessFunctions classLevelFitnessFunctions = project.getService(SettingsService.class).getClassLevelFitnessFunctions();
        Map<String, List<FitnessFunctionItem>> savedProfiles = classLevelFitnessFunctions.getProfiles();
        Set<FitnessFunction> profiles = new HashSet<>();
        for (Map.Entry<String, List<FitnessFunctionItem>> entry : savedProfiles.entrySet()) {
            Map<MetricType, Range> profileMap = new HashMap<>();
            for (FitnessFunctionItem item : entry.getValue()) {
                if (item.isLong()) {
                    profileMap.put(MetricType.valueOf(item.getName()),
                            DerivativeMetricsRange.of(Value.of(item.getMinLongValue()), Value.of(item.getMaxLongValue())));
                } else {
                    profileMap.put(MetricType.valueOf(item.getName()),
                            DerivativeMetricsRange.of(Value.of(item.getMinDoubleValue()), Value.of(item.getMaxDoubleValue())));
                }
            }
            FitnessFunction profile = new FitnessFunction(entry.getKey(), MetricLevel.CLASS, profileMap);
            profiles.add(profile);
        }
        return profiles;
    }
}
