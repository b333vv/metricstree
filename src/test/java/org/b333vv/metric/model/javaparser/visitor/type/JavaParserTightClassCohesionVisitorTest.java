package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserTightClassCohesionVisitorTest extends BaseVisitorTest {

    @Test
    public void testTCC() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("testData/cohesion/TccTest.java")).getResult().get();
        ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserTightClassCohesionVisitor visitor = new JavaParserTightClassCohesionVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(c, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0 / 3.0, metrics.get(0).getValue().doubleValue(), 0.01);
    }
}
