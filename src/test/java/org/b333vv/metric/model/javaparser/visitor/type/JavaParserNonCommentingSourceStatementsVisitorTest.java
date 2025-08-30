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

public class JavaParserNonCommentingSourceStatementsVisitorTest extends BaseVisitorTest {

    @Test
    public void testNCSS() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("testData/statements/NcssTest.java")).getResult().get();
        ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        JavaParserNonCommentingSourceStatementsVisitor visitor = new JavaParserNonCommentingSourceStatementsVisitor();
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(c, metrics::add);

        assertEquals(1, metrics.size());
        // Count only executable statements, exclude container blocks:
        // 1 (int a=1) + 1 (if) + 1 (println) = 3
        assertEquals(3.0, metrics.get(0).getValue().doubleValue());
    }
}
