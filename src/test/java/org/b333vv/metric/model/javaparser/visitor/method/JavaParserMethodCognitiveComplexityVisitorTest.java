package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.javaparser.visitor.type.BaseVisitorTest;
import org.b333vv.metric.model.metric.Metric;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserMethodCognitiveComplexityVisitorTest extends BaseVisitorTest {

    @Test
    public void testMCOGNITIVE_COMPLEXITY() throws IOException {
        String code = "class A { void a(int b) { " +
                "if (b > 0) { " +
                "  for (int i = 0; i < b; i++) { " +
                "    System.out.println(i); " +
                "  } " +
                "} " +
                "} }";
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration m = cu.findFirst(MethodDeclaration.class).get();

        JavaParserMethodCognitiveComplexityVisitor visitor = new JavaParserMethodCognitiveComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(m, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(3.0, metrics.get(0).getValue().doubleValue());
    }
}
