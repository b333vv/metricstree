package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserLinesOfCodeVisitorTest {
    @Test
    public void testLinesOfCode() {
        String code = "class A { \n" +
                "  void a() { \n" +
                "    System.out.println(\"Hello\"); \n" +
                "    System.out.println(\"World\"); \n" +
                "  } \n" +
                "}";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserLinesOfCodeVisitor visitor = new JavaParserLinesOfCodeVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        // The body is from line 2 to 5, so 4 lines.
        // Actually, it's from `{` to `}`.
        // line 2: {
        // line 3: System.out.println("Hello");
        // line 4: System.out.println("World");
        // line 5: }
        // The method itself is from line 2 to 5.
        // The body is from line 2 to 5.
        // Let's check the line numbers from the parser.
        // The body starts at line 2, ends at line 5. 5 - 2 + 1 = 4.
        assertEquals(4.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testEmptyMethod() {
        String code = "class A { void a() {} }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserLinesOfCodeVisitor visitor = new JavaParserLinesOfCodeVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testInterfaceMethod() {
        String code = "interface A { void a(); }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration methodDeclaration = cu.findFirst(MethodDeclaration.class).get();

        JavaParserLinesOfCodeVisitor visitor = new JavaParserLinesOfCodeVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(methodDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue());
    }
}
