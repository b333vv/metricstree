package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.*;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserNonCommentingSourceStatementsVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        // Use more precise counting that aligns with PSI implementation
        // Exclude empty statements and count only meaningful executable statements
        // This matches PSI's approach of excluding PsiComment and PsiEmptyStatement
        long ncss = n.findAll(Statement.class).stream()
            .filter(this::isCountableStatement)
            .count();
        
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
        
        // Note: JavaParser doesn't include comment nodes in Statement.class,
        // so we don't need to explicitly exclude comments like PSI does
        // Comments are handled separately in JavaParser's AST
        
        return true;
    }
}
