package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PieChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        JavaProject javaProject = new JavaProject("testProject");
        Project project = Mockito.mock(Project.class);

        PieChartDataCalculator calculator = new PieChartDataCalculator();
        List<MetricPieChartBuilder.PieChartStructure> result = calculator.calculate(javaProject, project);

        assertEquals(0, result.size());
    }
}