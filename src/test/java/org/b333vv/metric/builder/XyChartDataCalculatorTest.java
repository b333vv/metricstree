package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.ProjectElement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XyChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        ProjectElement javaProject = new ProjectElement("testProject");
        Project project = Mockito.mock(Project.class);

        XyChartDataCalculator calculator = new XyChartDataCalculator();
        XyChartDataCalculator.XyChartResult result = calculator.calculate(javaProject, project);

        assertNotNull(result);
        assertNotNull(result.getChart());
        assertNotNull(result.getInstability());
        assertNotNull(result.getAbstractness());
    }
}
