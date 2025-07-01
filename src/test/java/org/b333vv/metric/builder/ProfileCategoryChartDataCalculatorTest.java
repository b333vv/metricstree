package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.profile.FitnessFunction;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.CategoryChart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileCategoryChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        Map<FitnessFunction, Set<JavaClass>> classesByProfile = new HashMap<>();
        classesByProfile.put(new FitnessFunction("Profile1", "", ""), new HashSet<>());

        ProfileCategoryChartDataCalculator calculator = new ProfileCategoryChartDataCalculator();
        CategoryChart result = calculator.calculate(classesByProfile);

        assertNotNull(result);
    }
}
