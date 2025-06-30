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
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.CategoryChart;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CategoryChartDataCalculatorTest extends BaseTest {
    private CategoryChartDataCalculator calculator;
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        super.setUp();
        settingsService = mock(SettingsService.class);
        when(project.getService(SettingsService.class)).thenReturn(settingsService);
        Range mockedRange = mock(Range.class);
        when(mockedRange.getRangeType(Value.of(0.5))).thenReturn(RangeType.REGULAR);
        when(mockedRange.getRangeType(Value.of(0.7))).thenReturn(RangeType.HIGH);
        when(mockedRange.getRangeType(Value.of(0.9))).thenReturn(RangeType.VERY_HIGH);
        when(mockedRange.getRangeType(Value.of(1.1))).thenReturn(RangeType.EXTREME);
        when(settingsService.getRangeForMetric(MetricType.LCOM)).thenReturn(mockedRange);
        calculator = new CategoryChartDataCalculator(project);
    }

    @Test
    void calculateTest() {
        JavaClass javaClass1 = mock(JavaClass.class);
        Metric metric1 = mock(Metric.class);
        when(metric1.getType()).thenReturn(MetricType.LCOM);
        when(metric1.getValue()).thenReturn(Value.of(0.5));
        when(javaClass1.metric(MetricType.LCOM)).thenReturn(metric1);

        JavaClass javaClass2 = mock(JavaClass.class);
        Metric metric2 = mock(Metric.class);
        when(metric2.getType()).thenReturn(MetricType.LCOM);
        when(metric2.getValue()).thenReturn(Value.of(0.7));
        when(javaClass2.metric(MetricType.LCOM)).thenReturn(metric2);

        JavaClass javaClass3 = mock(JavaClass.class);
        Metric metric3 = mock(Metric.class);
        when(metric3.getType()).thenReturn(MetricType.LCOM);
        when(metric3.getValue()).thenReturn(Value.of(0.9));
        when(javaClass3.metric(MetricType.LCOM)).thenReturn(metric3);

        JavaClass javaClass4 = mock(JavaClass.class);
        Metric metric4 = mock(Metric.class);
        when(metric4.getType()).thenReturn(MetricType.LCOM);
        when(metric4.getValue()).thenReturn(Value.of(1.1));
        when(javaClass4.metric(MetricType.LCOM)).thenReturn(metric4);

        JavaProject javaProject = mock(JavaProject.class);
        when(javaProject.allClasses()).thenReturn(Stream.of(javaClass1, javaClass2, javaClass3, javaClass4));

        CategoryChart result = calculator.calculate(javaProject);

        assertNotNull(result);
    }
}
