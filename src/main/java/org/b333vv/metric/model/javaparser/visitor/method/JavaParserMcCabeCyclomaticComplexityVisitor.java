package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.function.Consumer;

public class JavaParserMcCabeCyclomaticComplexityVisitor extends JavaParserMethodVisitor {
    private int complexity;

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        complexity = 1;
        super.visit(n, collector);
        Metric metric = Metric.of(MetricType.CC, Value.of(complexity));
        collector.accept(metric);
    }

    @Override
    public void visit(IfStmt n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(ForStmt n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(ForEachStmt n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(WhileStmt n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(DoStmt n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(SwitchEntry n, Consumer<Metric> collector) {
        if (!n.getLabels().isEmpty()) { // check for case, not default
            complexity += n.getLabels().size();
        }
        super.visit(n, collector);
    }


    @Override
    public void visit(CatchClause n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(ConditionalExpr n, Consumer<Metric> collector) {
        complexity++;
        super.visit(n, collector);
    }

    @Override
    public void visit(BinaryExpr n, Consumer<Metric> collector) {
        if (n.getOperator() == BinaryExpr.Operator.AND || n.getOperator() == BinaryExpr.Operator.OR) {
            complexity++;
        }
        super.visit(n, collector);
    }
}
