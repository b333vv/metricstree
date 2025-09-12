package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.HeatMapChart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.b333vv.metric.model.metric.MetricLevel;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileHeatMapDataCalculatorTest {

    @Test
    public void testCalculate() {
        Map<FitnessFunction, Set<ClassElement>> classesByProfile = new HashMap<>();
        classesByProfile.put(new FitnessFunction("Profile1", MetricLevel.CLASS, new HashMap<>()), new HashSet<>());

        ProfileHeatMapDataCalculator calculator = new ProfileHeatMapDataCalculator();
        HeatMapChart result = calculator.calculate(classesByProfile);

        assertNotNull(result);
    }
}
