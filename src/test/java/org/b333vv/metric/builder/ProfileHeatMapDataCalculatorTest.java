package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.profile.FitnessFunction;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.HeatMapChart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileHeatMapDataCalculatorTest {

    @Test
    public void testCalculate() {
        Map<FitnessFunction, Set<JavaClass>> classesByProfile = new HashMap<>();
        classesByProfile.put(new FitnessFunction("Profile1", "", ""), new HashSet<>());

        ProfileHeatMapDataCalculator calculator = new ProfileHeatMapDataCalculator();
        HeatMapChart result = calculator.calculate(classesByProfile);

        assertNotNull(result);
    }
}
