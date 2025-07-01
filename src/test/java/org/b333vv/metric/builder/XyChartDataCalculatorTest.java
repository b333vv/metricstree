package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.XYChart;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XyChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        JavaProject javaProject = new JavaProject();
        Project project = Mockito.mock(Project.class);

        XyChartDataCalculator calculator = new XyChartDataCalculator();
        XYChart result = calculator.calculate(javaProject, project);

        assertNotNull(result);
    }
}
