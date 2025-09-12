package org.b333vv.metric.builder;

import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileTreeMapModelCalculatorTest {

    @Test
    public void testCalculate() {
        ProjectElement javaProject = new ProjectElement("testProject");

        ProfileTreeMapModelCalculator calculator = new ProfileTreeMapModelCalculator();
        MetricTreeMap<CodeElement> result = calculator.calculate(javaProject);

        assertNotNull(result);
    }
}
