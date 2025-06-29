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
import org.b333vv.metric.model.code.JavaPackage;
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
import org.b333vv.metric.ui.settings.fitnessfunction.PackageLevelFitnessFunctions;
import  org.b333vv.metric.util.SettingsService;

import java.util.*;

public class PackageLevelFitnessFunctionBuilder {
    public static Map<FitnessFunction, Set<JavaPackage>> packageLevelFitnessFunctionResult(Project project, JavaProject javaProject) {
        Map<FitnessFunction, Set<JavaPackage>> fitnessFunctionResult = new TreeMap<>();
        for (FitnessFunction profile : fitnessFunctionResult(project)) {
            Set<JavaPackage> packages = new HashSet<>();
            javaProject.allPackages()
                    .forEach(p -> {
                        if (checkPackage(p, profile)) {
                            packages.add(p);
                        }
                    });
            fitnessFunctionResult.put(profile, packages);
        }
        return Collections.unmodifiableMap(fitnessFunctionResult);
    }

    private static boolean checkPackage(JavaPackage javaPackage, FitnessFunction profile) {
        if (javaPackage.classes().toList().isEmpty()){
            return false;
        }
        for (Map.Entry<MetricType, Range> entry : profile.profile().entrySet()) {
            if (entry.getKey().level() == MetricLevel.PACKAGE) {
                Metric m = javaPackage.metric(entry.getKey());
                if (m != null
                        && entry.getValue().getRangeType(m.getValue()) != RangeType.REGULAR
                ) {
                    return false;
                }
            }
        }
        return true;
    }


    private static Set<FitnessFunction> fitnessFunctionResult(Project project) {
        PackageLevelFitnessFunctions packageLevelFitnessFunctions = project.getService(SettingsService.class).getPackageLevelFitnessFunctions();
        Map<String, List<FitnessFunctionItem>> fitnessFunction = packageLevelFitnessFunctions.getProfiles();
        Set<FitnessFunction> result = new HashSet<>();
        for (Map.Entry<String, List<FitnessFunctionItem>> entry : fitnessFunction.entrySet()) {
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
            FitnessFunction profile = new FitnessFunction(entry.getKey(), MetricLevel.PACKAGE, profileMap);
            result.add(profile);
        }
        return result;
    }
}
