package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.b333vv.metric.model.metric.MetricLevel;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileRadarDataCalculatorTest {

    @Test
    public void testCalculate() {
        Map<FitnessFunction, Set<JavaClass>> classesByProfile = new HashMap<>();
        classesByProfile.put(new FitnessFunction("Profile1", MetricLevel.CLASS, new HashMap<>()), new HashSet<>());
        Project project = Mockito.mock(Project.class);

        ProfileRadarDataCalculator calculator = new ProfileRadarDataCalculator();
        List<ProfileRadarChartBuilder.RadarChartStructure> result = calculator.calculate(classesByProfile, project);

        assertNotNull(result);
    }
}
