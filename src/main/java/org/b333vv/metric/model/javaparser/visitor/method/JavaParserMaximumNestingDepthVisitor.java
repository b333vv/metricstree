package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserMaximumNestingDepthVisitor extends JavaParserMethodVisitor {
    private int depth = 0;
    private int maxDepth = 0;

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        depth = 0;
        maxDepth = 0;
        super.visit(n, collector);
        Metric metric = Metric.of(MetricType.MND, Value.of(maxDepth));
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
    public void visit(ForStmt n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }

    @Override
    public void visit(ForEachStmt n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }

    @Override
    public void visit(WhileStmt n, Consumer<Metric> collector) {
        enter();
        super.visit(n, collector);
        exit();
    }

    @Override
    public void visit(DoStmt n, Consumer<Metric> collector) {
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
