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

public class JavaParserAccessToForeignDataVisitorTest extends BaseVisitorTest {

    @Test
    public void testATFD() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("testData/coupling/AtfdTest.java")).getResult().get();
        ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserAccessToForeignDataVisitor visitor = new JavaParserAccessToForeignDataVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(c, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue());
    }
}
