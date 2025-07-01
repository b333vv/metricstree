package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileTreeMapModelCalculatorTest {

    @Test
    public void testCalculate() {
        JavaProject javaProject = new JavaProject();

        ProfileTreeMapModelCalculator calculator = new ProfileTreeMapModelCalculator();
        MetricTreeMap<JavaCode> result = calculator.calculate(javaProject);

        assertNotNull(result);
    }
}
