package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserMcCabeCyclomaticComplexityVisitorTest {
    @Test
    public void testComplexity() {
        String code = "class A { void a() { " +
                "if (true) {} " + // +1
                "if (true && true) {} " + // +2
                "if (true || true) {} " + // +2
                "for (;;) {} " + // +1
                "while (true) {} " + // +1
                "do {} while (true); " + // +1
                "switch(1) { case 1: break; case 2: break; default: break;} " + // +2
                "try {} catch(Exception e) {} " + // +1
                "int a = true ? 1 : 0; " + // +1
                "} }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserMcCabeCyclomaticComplexityVisitor visitor = new JavaParserMcCabeCyclomaticComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1 + 1 + 2 + 2 + 1 + 1 + 1 + 2 + 1 + 1, metrics.get(0).getValue().longValue());
    }
}
