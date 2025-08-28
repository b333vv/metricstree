package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserInheritanceVisitorsTest extends BaseVisitorTest {

    @Test
    public void testDIT() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("testData/inheritance/C.java")).getResult().get();
        ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class, n -> n.getNameAsString().equals("C")).get();

        JavaParserDepthOfInheritanceTreeVisitor visitor = new JavaParserDepthOfInheritanceTreeVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(c, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(3.0, metrics.get(0).getValue().doubleValue()); // C -> B -> A -> Object
    }

    @Test
    public void testNOC() throws IOException {
        List<ClassOrInterfaceDeclaration> allClasses = Files.walk(Paths.get("testData/inheritance"))
                .filter(Files::isRegularFile)
                .map(p -> {
                    try {
                        return javaParser.parse(p).getResult().get();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                .collect(Collectors.toList());

        ClassOrInterfaceDeclaration a = allClasses.stream().filter(c -> c.getNameAsString().equals("A")).findFirst().get();

        JavaParserNumberOfChildrenVisitor visitor = new JavaParserNumberOfChildrenVisitor(allClasses);
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(a, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue()); // B extends A
    }

    @Test
    public void testNOOM() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("testData/inheritance/C.java")).getResult().get();
        ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class, n -> n.getNameAsString().equals("C")).get();

        JavaParserNumberOfOverriddenMethodsVisitor visitor = new JavaParserNumberOfOverriddenMethodsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(c, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue()); // m1
    }

    @Test
    public void testNOAM() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("testData/inheritance/C.java")).getResult().get();
        ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class, n -> n.getNameAsString().equals("C")).get();

        JavaParserNumberOfAddedMethodsVisitor visitor = new JavaParserNumberOfAddedMethodsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(c, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue()); // m3
    }
}
