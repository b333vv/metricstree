package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserHalsteadClassVisitorTest {
    @Test
    public void testHalsteadMetrics() {
        String code = "class A { int i; int sum(int a, int b) { return a + b; } }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        ClassOrInterfaceDeclaration classDeclaration = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserHalsteadClassVisitor visitor = new JavaParserHalsteadClassVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classDeclaration, metrics::add);

        assertEquals(6, metrics.size());

        // Let's trace it for "class A { int i; int sum(int a, int b) { return a + b; } }"
        // Operands: i, sum, a, b, a, b
        // n2 = 4 (i, sum, a, b), N2 = 6
        // Operators: int, int, (), int, int, return, +
        // n1 = 4, N1 = 7

        double volume = metrics.stream()
                .filter(m -> m.getType().name().equals("CHVL"))
                .findFirst().get().getValue().doubleValue();

        int n1 = 4; // int, (), return, +
        int n2 = 4; // i, sum, a, b
        int N1 = 7; // int, int, (), int, int, return, +
        int N2 = 6; // i, sum, a, b, a, b
        double expectedVolume = (double) (N1 + N2) * (Math.log(n1 + n2) / Math.log(2));

        assertEquals(expectedVolume, volume, 0.01);
    }
}
