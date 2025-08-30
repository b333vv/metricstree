package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JavaParserNonCommentingSourceStatementsVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        // Base: count executable statements (exclude EmptyStmt and BlockStmt), scoped to this class only
        long statements = n.findAll(Statement.class).stream()
            // exclude statements that belong to nested named classes (inner classes)
            .filter(stmt -> stmt.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(anc -> anc == n)
                    .orElse(true))
            .filter(this::isCountableStatement)
            .count();

        // Else branches count as +1 each
        long elseCount = n.findAll(IfStmt.class).stream()
                .filter(ifStmt -> ifStmt.getElseStmt().isPresent())
                .filter(ifStmt -> ifStmt.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == n)
                        .orElse(true))
                .count();

        // Switch entries (case/default) count as +1 each
        long switchEntries = n.findAll(SwitchEntry.class).stream()
                .filter(se -> se.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == n)
                        .orElse(true))
                .count();

        // Catch clauses and finally blocks
        long catchCount = n.findAll(CatchClause.class).stream()
                .filter(cc -> cc.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == n)
                        .orElse(true))
                .count();

        long finallyCount = n.findAll(TryStmt.class).stream()
                .filter(ts -> ts.getFinallyBlock().isPresent())
                .filter(ts -> ts.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == n)
                        .orElse(true))
                .count();

        // PSI counts a declaration in the for-loop initializer as a separate statement.
        // Mirror this by adding +1 for each ForStmt whose initializer contains a VariableDeclarationExpr.
        long forInitDecls = n.findAll(ForStmt.class).stream()
                .filter(fs -> fs.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == n)
                        .orElse(true))
                .filter(fs -> fs.getInitialization().stream().anyMatch(expr -> expr instanceof com.github.javaparser.ast.expr.VariableDeclarationExpr))
                .count();

        // PSI also counts update expressions in the for-loop header (e.g., i++, i+=1) as separate statements.
        // Mirror this by adding +1 per expression in the update section.
        long forUpdateExprs = n.findAll(ForStmt.class).stream()
                .filter(fs -> fs.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(anc -> anc == n)
                        .orElse(true))
                .mapToLong(fs -> fs.getUpdate().size())
                .sum();

        // Declarations: class/interface itself (+1), methods/constructors (+1 each), fields (+1 per variable declarator)
        long classDecl = 1; // count this declaration itself

        long methodDecls = n.getMembers().stream()
                .filter(m -> m instanceof MethodDeclaration)
                .count();

        long ctorDecls = n.getMembers().stream()
                .filter(m -> m instanceof ConstructorDeclaration)
                .count();

        long fieldDecls = n.getMembers().stream()
                .filter(m -> m instanceof FieldDeclaration)
                .map(m -> (FieldDeclaration) m)
                .map(fd -> fd.getVariables().size())
                .collect(Collectors.summingInt(Integer::intValue));

        long ncss = statements + elseCount + switchEntries + catchCount + finallyCount + forInitDecls + forUpdateExprs
                + classDecl + methodDecls + ctorDecls + fieldDecls;
        
        Metric metric = Metric.of(MetricType.NCSS, Value.of(ncss));
        collector.accept(metric);
    }
    
    /**
     * Determines if a statement should be counted for NCSS.
     * Aligns with PSI's exclusion of comments and empty statements.
     */
    private boolean isCountableStatement(Statement stmt) {
        // Exclude empty statements (equivalent to PSI's PsiEmptyStatement exclusion)
        if (stmt instanceof EmptyStmt) {
            return false;
        }

        // Exclude container blocks to mirror PSI behavior which does not count
        // block bodies (method bodies, if/else bodies, loop bodies, try/catch/finally bodies)
        if (stmt instanceof BlockStmt) {
            return false;
        }

        // Exclude statements that are the body of an expression-bodied lambda.
        java.util.Optional<LambdaExpr> lambda = stmt.findAncestor(LambdaExpr.class);
        if (lambda.isPresent()) {
            if (!(lambda.get().getBody() instanceof BlockStmt)) {
                return false;
            }
        }

        // Comments and switch entries are not part of Statement in JavaParser,
        // so no need to exclude them explicitly
        return true;
    }
}
