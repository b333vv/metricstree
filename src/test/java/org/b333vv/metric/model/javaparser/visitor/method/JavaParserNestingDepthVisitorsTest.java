package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNestingDepthVisitorsTest {
    private MethodDeclaration methodDeclaration;

    @BeforeEach
    public void setup() {
        String code = "class A { void a() { " +
                "if (true) { " + // CND 1, MND 1
                "  for (;;) { " + // LND 1, MND 2
                "    if (true) { " + // CND 2, MND 3
                "      while(true) {} " + // LND 2, MND 4
                "    } " +
                "  } " +
                "} " +
                "if (true) {} " + // CND 1, MND 1 (not max)
                "} }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        methodDeclaration = cu.findFirst(MethodDeclaration.class).get();
    }

    @Test
    public void testMaximumNestingDepth() {
        JavaParserMaximumNestingDepthVisitor visitor = new JavaParserMaximumNestingDepthVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(4.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testConditionNestingDepth() {
        JavaParserConditionNestingDepthVisitor visitor = new JavaParserConditionNestingDepthVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(2.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testLoopNestingDepth() {
        JavaParserLoopNestingDepthVisitor visitor = new JavaParserLoopNestingDepthVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(2.0, metrics.get(0).getValue().doubleValue());
    }
}
