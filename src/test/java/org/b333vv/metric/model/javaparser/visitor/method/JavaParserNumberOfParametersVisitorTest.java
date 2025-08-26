package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNumberOfParametersVisitorTest {
    @Test
    public void testNumberOfParameters() {
        String code = "class A { void a(int i, String s, double d) { } }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserNumberOfParametersVisitor visitor = new JavaParserNumberOfParametersVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(3.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testNoParameters() {
        String code = "class A { void a() { } }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserNumberOfParametersVisitor visitor = new JavaParserNumberOfParametersVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(0.0, metrics.get(0).getValue().doubleValue());
    }
}
