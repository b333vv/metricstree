package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNumberOfAccessorMethodsVisitorTest {
    @Test
    public void testNumberOfAccessorMethods() {
        String code = "class A { " +
                "int i; " +
                "public int getI() { return i; } " +
                "public void setI(int i) { this.i = i; } " +
                "public void justAMethod() {}" +
                "public String getS() { return \"s\"; }" +
                "public void setS(String s) {}" +
                "}";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserNumberOfAccessorMethodsVisitor visitor = new JavaParserNumberOfAccessorMethodsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classOrInterfaceDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(4.0, metrics.get(0).getValue().doubleValue());
    }

    @Test
    public void testNoAccessorMethods() {
        String code = "class A { " +
                "int i; " +
                "public void justAMethod() {}" +
                "}";
        JavaParser javaParser = new JavaParser();
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserNumberOfAccessorMethodsVisitor visitor = new JavaParserNumberOfAccessorMethodsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classOrInterfaceDeclaration, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(0.0, metrics.get(0).getValue().doubleValue());
    }
}
