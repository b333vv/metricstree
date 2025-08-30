package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParserNCSSJavaClassDiagnosticsTest extends BaseVisitorTest {

    @Test
    public void printJavaClassNCSSBreakdown() throws IOException {
        CompilationUnit cu = javaParser.parse(Paths.get("src/main/java/org/b333vv/metric/model/code/JavaClass.java")).getResult().get();
        ClassOrInterfaceDeclaration javaClassDecl = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(d -> d.getNameAsString().equals("JavaClass"))
                .findFirst()
                .orElseThrow();

        var allStatements = javaClassDecl.findAll(Statement.class).stream()
                // restrict to statements within this class (exclude nested named classes)
                .filter(stmt -> stmt.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == javaClassDecl)
                        .orElse(true))
                // mirror visitor filters
                .filter(stmt -> !(stmt instanceof EmptyStmt))
                .filter(stmt -> !(stmt instanceof BlockStmt))
                .collect(Collectors.toList());

        Map<String, Long> byType = allStatements.stream()
                .collect(Collectors.groupingBy(s -> s.getClass().getSimpleName(), Collectors.counting()));

        String breakdown = byType.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));

        assertEquals(20, allStatements.size(),
                () -> "Expected NCSS=20 for JavaClass after filtering, but got " + allStatements.size() +
                        "\nBreakdown by Statement type:\n" + breakdown);
    }
}
