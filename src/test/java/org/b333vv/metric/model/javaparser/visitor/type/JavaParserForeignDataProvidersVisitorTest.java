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

public class JavaParserForeignDataProvidersVisitorTest extends BaseVisitorTest {

    @Test
    public void testFDP() throws IOException {
        List<ClassOrInterfaceDeclaration> allClasses = Files.walk(Paths.get("testData/coupling"))
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

        ClassOrInterfaceDeclaration foreignData = allClasses.stream().filter(c -> c.getNameAsString().equals("ForeignData")).findFirst().get();

        JavaParserForeignDataProvidersVisitor visitor = new JavaParserForeignDataProvidersVisitor(allClasses);
        List<Metric> metrics = new ArrayList<>();
        visitor.visit(foreignData, metrics::add);

        assertEquals(1, metrics.size());
        assertEquals(1.0, metrics.get(0).getValue().doubleValue());
    }
}
