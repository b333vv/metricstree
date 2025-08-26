package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.*;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserCognitiveComplexityVisitor extends JavaParserMethodVisitor {
    private int complexity = 0;
    private int nesting = 0;

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        complexity = 0;
        nesting = 0;
        super.visit(n, collector);
        Metric metric = Metric.of(MetricType.CCM, Value.of(complexity));
        collector.accept(metric);
    }

    @Override
    public void visit(IfStmt n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(ForStmt n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(ForEachStmt n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(WhileStmt n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(DoStmt n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(CatchClause n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(SwitchStmt n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(BreakStmt n, Consumer<Metric> collector) {
        if (n.getLabel().isPresent()) {
            complexity++;
        }
        super.visit(n, collector);
    }

    @Override
    public void visit(ContinueStmt n, Consumer<Metric> collector) {
        if (n.getLabel().isPresent()) {
            complexity++;
        }
        super.visit(n, collector);
    }

    @Override
    public void visit(ConditionalExpr n, Consumer<Metric> collector) {
        complexity += 1 + nesting;
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(LambdaExpr n, Consumer<Metric> collector) {
        nesting++;
        super.visit(n, collector);
        nesting--;
    }

    @Override
    public void visit(BinaryExpr n, Consumer<Metric> collector) {
        BinaryExpr.Operator operator = n.getOperator();
        if (operator == BinaryExpr.Operator.AND || operator == BinaryExpr.Operator.OR) {
            boolean parentIsSameOp = n.getParentNode().map(p -> {
                if (p instanceof BinaryExpr) {
                    return ((BinaryExpr) p).getOperator() == operator;
                }
                return false;
            }).orElse(false);

            if (!parentIsSameOp) {
                complexity++;
            }
        }
        super.visit(n, collector);
    }
}
