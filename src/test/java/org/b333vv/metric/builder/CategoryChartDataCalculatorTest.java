package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.CategoryChart;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CategoryChartDataCalculatorTest {

    @Test
    public void testCalculate() {
        JavaProject javaProject = new JavaProject();
        Project project = Mockito.mock(Project.class);

        CategoryChartDataCalculator calculator = new CategoryChartDataCalculator();
        CategoryChart result = calculator.calculate(javaProject, project);

        assertNotNull(result);
    }
}