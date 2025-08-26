package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserCognitiveComplexityVisitorTest {
    @Test
    public void testCognitiveComplexity() {
        String code = "class A { void a(int b) { " +
                "if (b > 0) { " + // +1
                "  for (int i = 0; i < b; i++) { " + // +2 (nesting +1)
                "    System.out.println(i); " +
                "  } " +
                "} " +
                "} }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserCognitiveComplexityVisitor visitor = new JavaParserCognitiveComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1 + 2, metrics.get(0).getValue().longValue());
    }

    @Test
    public void testCognitiveComplexityWithElse() {
        String code = "class A { void a(int b) { " +
                "if (b > 0) { " + // +1
                "  System.out.println();" +
                "} else { " + // +1
                "  System.out.println();" +
                "} " +
                "} }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserCognitiveComplexityVisitor visitor = new JavaParserCognitiveComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1 + 1, metrics.get(0).getValue().longValue());
    }

     @Test
    public void testCognitiveComplexityWithLogic() {
        String code = "class A { void a(boolean b, boolean c, boolean d) { " +
                "if (b && c || d) { " + // +1 (if) +1 (&&) +1 (||)
                "  System.out.println();" +
                "} " +
                "} }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserCognitiveComplexityVisitor visitor = new JavaParserCognitiveComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        // Cognitive complexity increases for each logical operator in a sequence.
        // `b && c` is one sequence (+1 for &&). `... || d` is a different operator, so it's a new sequence (+1 for ||).
        // The `if` itself adds +1. Total = 1 (if) + 1 (&&) + 1 (||) = 3.
        assertEquals(3, metrics.get(0).getValue().longValue());
    }
}
