package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNumberOfAttributesVisitorTest {
    @Test
    public void testNumberOfAttributes() {
        String code = "class A { int i; String s; double d; }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserNumberOfAttributesVisitor visitor = new JavaParserNumberOfAttributesVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classOrInterfaceDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(3.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testNoAttributes() {
        String code = "class A { }";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserNumberOfAttributesVisitor visitor = new JavaParserNumberOfAttributesVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classOrInterfaceDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(0.0, metrics.get(0).getValue().doubleValue());
    }
}
