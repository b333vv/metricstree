package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.profile.FitnessFunction;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileBoxChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        Map<FitnessFunction, Set<JavaClass>> classesByProfile = new HashMap<>();
        classesByProfile.put(new FitnessFunction("Profile1", "", ""), new HashSet<>());

        ProfileBoxChartDataCalculator calculator = new ProfileBoxChartDataCalculator();
        List<ProfileBoxChartBuilder.BoxChartStructure> result = calculator.calculate(classesByProfile);

        assertEquals(1, result.size());
    }
}
