package org.b333vv.metric.research.complexity;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.research.MetricVerificationTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaParserNCSSJavaClassDiagnosticsTest extends MetricVerificationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Use the shared test data like other verification tests
        setupTest("com/verification/complexity/NCSSTestCases.java");
    }

    private void printPsiBreakdown(String className) {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.getFile();
        PsiClass target = java.util.Arrays.stream(psiFile.getClasses())
                .filter(c -> className.equals(c.getName()))
                .findFirst().orElse(null);
        if (target == null) return;

        var all = PsiTreeUtil.findChildrenOfType(target, PsiStatement.class).stream()
                .filter(stmt -> !(stmt instanceof PsiEmptyStatement))
                .filter(stmt -> !(stmt instanceof PsiBlockStatement))
                .filter(stmt -> !(stmt instanceof PsiSwitchLabelStatement))
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Long> byType = all.stream().collect(
                java.util.stream.Collectors.groupingBy(s -> s.getClass().getSimpleName(), java.util.stream.Collectors.counting())
        );
        System.out.println("=== PSI NCSS breakdown for JavaClass ===");
        System.out.println("Total count: " + all.size());
        byType.entrySet().stream()
                .sorted(java.util.Comparator.comparing(java.util.Map.Entry::getKey))
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
        System.out.println("========================================");

        // Detailed list for diffing with JP
        System.out.println("--- PSI Statement details (type: text) ---");
        for (PsiStatement s : all) {
            String text = s.getText().replace("\n", " ").trim();
            if (text.length() > 120) text = text.substring(0, 120) + "...";
            System.out.println(s.getClass().getSimpleName() + ": " + text);
        }
        System.out.println("------------------------------------------");
    }
    public void testNCSS_PSI_vs_JavaParser_for_JavaClass() throws Exception {
        final String className = "NCSS_TestClass";
        var psiValue = getPsiValue(className, MetricType.NCSS);
        var jpValue = getJavaParserValue(className, MetricType.NCSS);
        System.out.println("PSI NCSS(" + className + "): " + psiValue);
        System.out.println("JP  NCSS(" + className + "): " + jpValue);

        int psi = (int) Math.round(psiValue.doubleValue());
        int jp = (int) Math.round(jpValue.doubleValue());

        if (psi != jp) {
            // Print a JavaParser-side breakdown to identify extra statements and components
            printJavaParserBreakdown(className);
            printJavaParserComponents(className);
            printPsiComponents(className);
            printPsiBreakdown(className);
        }

        // Assert equality to lock the expectation
        assertEquals("JavaParser NCSS must match PSI for " + className, psi, jp);
    }

    private void printJavaParserBreakdown(String className) throws Exception {
        // Setup a JavaParser with symbol solving rooted at the project src
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File("metric-verification-data/src/main/java")));
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolver));
        JavaParser parser = new JavaParser(config);

        CompilationUnit cu = parser.parse(Paths.get("metric-verification-data/src/main/java/com/verification/complexity/NCSSTestCases.java"))
                .getResult().orElseThrow();
        ClassOrInterfaceDeclaration javaClassDecl = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(d -> d.getNameAsString().equals(className))
                .findFirst()
                .orElseThrow();

        var allStatements = javaClassDecl.findAll(Statement.class).stream()
                // keep only statements whose nearest class ancestor is this class (exclude nested named classes)
                .filter(stmt -> stmt.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == javaClassDecl)
                        .orElse(true))
                // mirror NCSS filtering: exclude EmptyStmt and BlockStmt
                .filter(stmt -> !(stmt instanceof EmptyStmt))
                .filter(stmt -> !(stmt instanceof BlockStmt))
                .collect(Collectors.toList());

        Map<String, Long> byType = allStatements.stream()
                .collect(Collectors.groupingBy(s -> s.getClass().getSimpleName(), Collectors.counting()));

        System.out.println("=== JavaParser NCSS breakdown for JavaClass ===");
        System.out.println("Total count: " + allStatements.size());
        byType.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
        System.out.println("==============================================");

        var exprStmts = allStatements.stream()
                .filter(s -> s instanceof com.github.javaparser.ast.stmt.ExpressionStmt)
                .map(s -> (com.github.javaparser.ast.stmt.ExpressionStmt) s)
                .collect(Collectors.toList());
        if (!exprStmts.isEmpty()) {
            System.out.println("--- JP ExpressionStmt details (code@range) ---");
            for (var es : exprStmts) {
                String code = es.toString().replace("\n", "\\n");
                String range = es.getRange().map(r -> r.begin.line + ":" + r.begin.column + "-" + r.end.line + ":" + r.end.column).orElse("<no-range>");
                System.out.println(code + " @ " + range);
            }
            System.out.println("----------------------------------------------");
        }
    }

    private void printJavaParserComponents(String className) throws Exception {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File("metric-verification-data/src/main/java")));
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolver));
        JavaParser parser = new JavaParser(config);

        CompilationUnit cu = parser.parse(Paths.get("metric-verification-data/src/main/java/com/verification/complexity/NCSSTestCases.java"))
                .getResult().orElseThrow();
        ClassOrInterfaceDeclaration c = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(d -> d.getNameAsString().equals(className))
                .findFirst()
                .orElseThrow();

        long statements = c.findAll(Statement.class).stream()
                .filter(stmt -> stmt.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .filter(stmt -> !(stmt instanceof EmptyStmt))
                .filter(stmt -> !(stmt instanceof BlockStmt))
                .count();

        long elseCount = c.findAll(com.github.javaparser.ast.stmt.IfStmt.class).stream()
                .filter(ifStmt -> ifStmt.getElseStmt().isPresent())
                .filter(ifStmt -> ifStmt.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .count();

        long switchEntries = c.findAll(com.github.javaparser.ast.stmt.SwitchEntry.class).stream()
                .filter(se -> se.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .count();

        long catchCount = c.findAll(com.github.javaparser.ast.stmt.CatchClause.class).stream()
                .filter(cc -> cc.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .count();

        long finallyCount = c.findAll(com.github.javaparser.ast.stmt.TryStmt.class).stream()
                .filter(ts -> ts.getFinallyBlock().isPresent())
                .filter(ts -> ts.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .count();

        long classDecl = 1;
        long methodDecls = c.getMembers().stream().filter(m -> m instanceof com.github.javaparser.ast.body.MethodDeclaration).count();
        long ctorDecls = c.getMembers().stream().filter(m -> m instanceof com.github.javaparser.ast.body.ConstructorDeclaration).count();
        long fieldDecls = c.getMembers().stream()
                .filter(m -> m instanceof com.github.javaparser.ast.body.FieldDeclaration)
                .map(m -> (com.github.javaparser.ast.body.FieldDeclaration) m)
                .mapToInt(fd -> fd.getVariables().size())
                .sum();

        long forInitDecls = c.findAll(com.github.javaparser.ast.stmt.ForStmt.class).stream()
                .filter(fs -> fs.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .filter(fs -> fs.getInitialization().stream().anyMatch(expr -> expr instanceof com.github.javaparser.ast.expr.VariableDeclarationExpr))
                .count();

        long forInitDeclVars = c.findAll(com.github.javaparser.ast.stmt.ForStmt.class).stream()
                .filter(fs -> fs.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .flatMap(fs -> fs.getInitialization().stream())
                .filter(expr -> expr instanceof com.github.javaparser.ast.expr.VariableDeclarationExpr)
                .map(expr -> (com.github.javaparser.ast.expr.VariableDeclarationExpr) expr)
                .mapToInt(vde -> vde.getVariables().size())
                .sum();

        long forUpdateExprs = c.findAll(com.github.javaparser.ast.stmt.ForStmt.class).stream()
                .filter(fs -> fs.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == c)
                        .orElse(true))
                .mapToLong(fs -> fs.getUpdate().size())
                .sum();

        long total = statements + elseCount + switchEntries + catchCount + finallyCount + forInitDecls + forUpdateExprs
                + classDecl + methodDecls + ctorDecls + fieldDecls;

        System.out.println("=== JavaParser NCSS components (used for total) ===");
        System.out.println("statements=" + statements);
        System.out.println("elseCount=" + elseCount);
        System.out.println("switchEntries=" + switchEntries);
        System.out.println("catchCount=" + catchCount);
        System.out.println("finallyCount=" + finallyCount);
        System.out.println("classDecl=" + classDecl);
        System.out.println("forInitDecls=" + forInitDecls);
        System.out.println("forInitDeclVars=" + forInitDeclVars);
        System.out.println("forUpdateExprs=" + forUpdateExprs);
        System.out.println("methodDecls=" + methodDecls);
        System.out.println("ctorDecls=" + ctorDecls);
        System.out.println("fieldDecls=" + fieldDecls);
        System.out.println("Computed total (JP)=" + total);
        System.out.println("===============================================");
    }

    private void printPsiComponents(String className) {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.getFile();
        PsiClass target = java.util.Arrays.stream(psiFile.getClasses())
                .filter(c -> className.equals(c.getName()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            System.out.println("[PSI] Target class not found: " + className);
            return;
        }

        long baseStatements = PsiTreeUtil.findChildrenOfType(target, PsiStatement.class).stream()
                .filter(stmt -> !(stmt instanceof PsiEmptyStatement))
                .filter(stmt -> !(stmt instanceof PsiBlockStatement))
                .filter(stmt -> !(stmt instanceof PsiSwitchLabelStatement))
                .count();

        long elseCount = PsiTreeUtil.findChildrenOfType(target, PsiIfStatement.class).stream()
                .filter(i -> i.getElseBranch() != null)
                .count();

        long switchEntries = PsiTreeUtil.findChildrenOfType(target, PsiSwitchLabelStatement.class).size();

        long catchCount = PsiTreeUtil.findChildrenOfType(target, PsiCatchSection.class).size();

        long finallyCount = PsiTreeUtil.findChildrenOfType(target, PsiTryStatement.class).stream()
                .filter(t -> t.getFinallyBlock() != null)
                .count();

        long classDecl = 1;
        long methodDecls = PsiTreeUtil.findChildrenOfType(target, PsiMethod.class).stream()
                .filter(m -> !m.isConstructor())
                .count();
        long ctorDecls = PsiTreeUtil.findChildrenOfType(target, PsiMethod.class).stream()
                .filter(PsiMethod::isConstructor)
                .count();
        long fieldDecls = PsiTreeUtil.findChildrenOfType(target, PsiField.class).size();

        long total = baseStatements + elseCount + switchEntries + catchCount + finallyCount
                + classDecl + methodDecls + ctorDecls + fieldDecls;

        System.out.println("=== PSI NCSS components (computed in test) ===");
        System.out.println("baseStatements=" + baseStatements);
        System.out.println("elseCount=" + elseCount);
        System.out.println("switchEntries=" + switchEntries);
        System.out.println("catchCount=" + catchCount);
        System.out.println("finallyCount=" + finallyCount);
        System.out.println("classDecl=" + classDecl);
        System.out.println("methodDecls=" + methodDecls);
        System.out.println("ctorDecls=" + ctorDecls);
        System.out.println("fieldDecls=" + fieldDecls);
        System.out.println("Computed total (PSI)=" + total);
        System.out.println("===========================================");
    }
}
