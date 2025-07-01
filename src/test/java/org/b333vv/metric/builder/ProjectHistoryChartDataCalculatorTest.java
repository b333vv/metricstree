package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.XYChart;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProjectHistoryChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        Project project = Mockito.mock(Project.class);

        ProjectHistoryChartDataCalculator calculator = new ProjectHistoryChartDataCalculator();
        XYChart result = calculator.calculate(project);

        assertNotNull(result);
    }
}
