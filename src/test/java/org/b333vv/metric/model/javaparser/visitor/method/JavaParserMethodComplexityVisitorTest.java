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

public class JavaParserMethodComplexityVisitorTest extends BaseVisitorTest {

    @Test
    public void testMCOMP() throws IOException {
        String code = "class A { void a() { " +
                "if (true) {} " +
                "if (true && true) {} " +
                "for (;;) {} " +
                "} }";
        CompilationUnit cu = javaParser.parse(code).getResult().get();
        MethodDeclaration m = cu.findFirst(MethodDeclaration.class).get();

        JavaParserMethodComplexityVisitor visitor = new JavaParserMethodComplexityVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(m, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1 + 1 + 2 + 1, metrics.get(0).getValue().doubleValue());
    }
}
