package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserHalsteadMethodVisitorTest {
    @Test
    public void testHalsteadMetrics() {
        String code = "class A { int sum(int a, int b) { return a + b; } }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserHalsteadMethodVisitor visitor = new JavaParserHalsteadMethodVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(6, metrics.size());

        // Let's trace it for "int sum(int a, int b) { return a + b; }"
        // Operands: sum, a, b, a, b --> n2=3, N2=5
        // Operators: int, (), int, int, return, + --> n1=4, N1=6

        double volume = metrics.stream()
                .filter(m -> m.getType().name().equals("HVL"))
                .findFirst().get().getValue().doubleValue();

        int n1 = 4; // int, (), return, +
        int n2 = 3; // sum, a, b
        int N1 = 6; // int, (), int, int, return, +
        int N2 = 5; // sum, a, b, a, b
        double expectedVolume = (double) (N1 + N2) * (Math.log(n1 + n2) / Math.log(2));

        assertEquals(expectedVolume, volume, 0.01);
    }
}
