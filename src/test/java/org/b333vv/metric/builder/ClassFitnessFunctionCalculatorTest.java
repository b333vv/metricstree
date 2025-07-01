package org.b333vv.metric.builder;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;

import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClassFitnessFunctionCalculatorTest {

    @Mock
    private Project mockProject;
    @Mock
    private ProgressIndicator mockIndicator;
    @Mock
    private JavaProject mockJavaProject;

    private ClassFitnessFunctionCalculator calculator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        calculator = new ClassFitnessFunctionCalculator();
        
        
    }

    @Test
    public void testCalculate() {
        // Mock the static method call
        // This is tricky with static methods, usually you'd refactor to make it non-static or use PowerMock/JMockit
        // For now, assuming ClassLevelFitnessFunctionBuilder.classesByMetricsProfileDistribution returns a non-null map
        // In a real scenario, you'd need to ensure this static method is testable or mockable.
        // For the purpose of this test, we'll assume it works as expected and focus on the calculator's interaction.

        Map<FitnessFunction, Set<JavaClass>> result = calculator.calculate(mockProject, mockJavaProject);

        assertNotNull(result);
        // Further assertions can be added if the static method's behavior can be controlled or verified.
    }
}
