package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNumberOfChildrenVisitorTest extends BaseVisitorTest {

    private List<ClassOrInterfaceDeclaration> allClasses;

    @BeforeEach
    public void setup() {
        super.setup();
        try {
            allClasses = Files.walk(Paths.get("testData/inheritance"))
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            return javaParser.parse(p).getResult().get();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testNOCForClassA() {
        ClassOrInterfaceDeclaration classA = findClass("A");
        JavaParserNumberOfChildrenVisitor visitor = new JavaParserNumberOfChildrenVisitor(allClasses);
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classA, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue(), "Class A should have 1 child (B)");
    }

    @Test
    public void testNOCForClassB() {
        ClassOrInterfaceDeclaration classB = findClass("B");
        JavaParserNumberOfChildrenVisitor visitor = new JavaParserNumberOfChildrenVisitor(allClasses);
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classB, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue(), "Class B should have 1 child (C)");
    }

    @Test
    public void testNOCForClassC() {
        ClassOrInterfaceDeclaration classC = findClass("C");
        JavaParserNumberOfChildrenVisitor visitor = new JavaParserNumberOfChildrenVisitor(allClasses);
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(classC, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(0.0, metrics.get(0).getValue().doubleValue(), "Class C should have 0 children");
    }

    private ClassOrInterfaceDeclaration findClass(String name) {
        return allClasses.stream().filter(c -> c.getNameAsString().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Class " + name + " not found"));
    }
}
