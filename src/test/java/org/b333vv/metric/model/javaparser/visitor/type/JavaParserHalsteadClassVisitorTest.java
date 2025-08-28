package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaParserHalsteadClassVisitorTest {
    @Test
    public void testHalsteadMetrics() {
        String code = "class A { int i = 1; }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        ClassOrInterfaceDeclaration classDeclaration = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserHalsteadClassVisitor visitor = new JavaParserHalsteadClassVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classDeclaration, metrics::add);

        Map<MetricType, Metric> metricMap = metrics.stream()
                .collect(Collectors.toMap(Metric::getType, Function.identity()));

        // Manual Calculation for "class A { int i = 1; }"
        // Operands: i, 1. (n2=2, N2=2)
        // Operators: int, =. (n1=2, N1=2)
        int n1 = 2;
        int n2 = 2;
        int N1 = 2;
        int N2 = 2;

        long vocabulary = n1 + n2; // 4
        long length = N1 + N2; // 4
        double volume = length * (Math.log(vocabulary) / Math.log(2)); // 4 * log2(4) = 8
        double difficulty = ((double) n1 / 2.0) * ((double) N2 / (double) n2); // (2/2) * (2/2) = 1.0
        double effort = difficulty * volume; // 1.0 * 8 = 8
        double errors = volume / 3000.0; // 8 / 3000

        assertNotNull(metricMap.get(MetricType.CHVC));
        assertEquals(vocabulary, metricMap.get(MetricType.CHVC).getValue().longValue());

        assertNotNull(metricMap.get(MetricType.CHL));
        assertEquals(length, metricMap.get(MetricType.CHL).getValue().longValue());

        assertNotNull(metricMap.get(MetricType.CHVL));
        assertEquals(volume, metricMap.get(MetricType.CHVL).getValue().doubleValue(), 0.01);

        assertNotNull(metricMap.get(MetricType.CHD));
        assertEquals(difficulty, metricMap.get(MetricType.CHD).getValue().doubleValue(), 0.01);

        assertNotNull(metricMap.get(MetricType.CHEF));
        assertEquals(effort, metricMap.get(MetricType.CHEF).getValue().doubleValue(), 0.01);

        assertNotNull(metricMap.get(MetricType.CHER));
        assertEquals(errors, metricMap.get(MetricType.CHER).getValue().doubleValue(), 0.001);
    }
}
