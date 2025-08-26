package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNumberOfLoopsVisitorTest {
    @Test
    public void testNumberOfLoops() {
        String code = "class A { void a() { for(;;); while(true); do {} while(true); for(Object o: new java.util.ArrayList<>()); } }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserNumberOfLoopsVisitor visitor = new JavaParserNumberOfLoopsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(4.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testNoLoops() {
        String code = "class A { void a() { System.out.println(\"Hello\"); } }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserNumberOfLoopsVisitor visitor = new JavaParserNumberOfLoopsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(0.0, metrics.get(0).getValue().doubleValue());
    }
}
