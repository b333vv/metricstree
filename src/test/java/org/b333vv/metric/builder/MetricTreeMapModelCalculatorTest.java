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

import com.intellij.openapi.progress.ProgressIndicator;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricTreeMapModelCalculatorTest extends BaseTest {
    private MetricTreeMapModelCalculator calculator;
    private MetricTaskManager metricTaskManager;

    @BeforeEach
    void setUp() {
        super.setUp();
        metricTaskManager = mock(MetricTaskManager.class);
        when(project.getService(MetricTaskManager.class)).thenReturn(metricTaskManager);
        calculator = new MetricTreeMapModelCalculator(project);
    }

    @Test
    void calculateTest() {
        ProgressIndicator indicator = mock(ProgressIndicator.class);
        JavaProject javaProject = mock(JavaProject.class);
        when(metricTaskManager.getClassAndMethodModel(indicator)).thenReturn(javaProject);

        MetricTreeMap<JavaCode> result = calculator.calculate(indicator);

        assertNotNull(result);
    }
}
