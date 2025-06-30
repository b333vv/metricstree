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
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;

import java.util.Map;

public class PieChartDataCalculator {
    private final Project project;

    public PieChartDataCalculator(Project project) {
        this.project = project;
    }

    public Map<MetricType, Map<RangeType, Double>> calculate(JavaProject javaProject) {
        ClassesByMetricsValuesCounter counter = new ClassesByMetricsValuesCounter(project);
        return counter.classesByMetricsValuesDistribution(javaProject);
    }
}
