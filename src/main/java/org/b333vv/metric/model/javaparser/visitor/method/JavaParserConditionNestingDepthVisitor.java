package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserConditionNestingDepthVisitor extends JavaParserMethodVisitor {
    private int depth = 0;
    private int maxDepth = 0;

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        depth = 0;
        maxDepth = 0;
        super.visit(n, collector);
        Metric metric = Metric.of(MetricType.CND, Value.of(maxDepth));
        collector.accept(metric);
    }

    private void enter() {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    private void exit() {
        depth--;
    }

    @Override
    public void visit(IfStmt n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }

    @Override
    public void visit(SwitchStmt n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }

    @Override
    public void visit(CatchClause n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }

    @Override
    public void visit(ConditionalExpr n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }
}
