package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.*;
import java.util.function.Consumer;

public class JavaParserHalsteadMethodVisitor extends JavaParserMethodVisitor {
    private final Set<String> operators = new HashSet<>();
    private final Set<String> operands = new HashSet<>();
    private final List<String> operatorList = new ArrayList<>();
    private final List<String> operandList = new ArrayList<>();

    @Override
    public void visit(MethodDeclaration n, Consumer<Metric> collector) {
        operators.clear();
        operands.clear();
        operatorList.clear();
        operandList.clear();

        super.visit(n, collector);

        int n1 = operators.size();
        int n2 = operands.size();
        int N1 = operatorList.size();
        int N2 = operandList.size();

        if (n1 > 0 && n2 > 0 && N1 > 0 && N2 > 0) {
            collector.accept(createMetric(MetricType.HVL, (double) (N1 + N2) * (Math.log(n1 + n2) / Math.log(2))));
            collector.accept(createMetric(MetricType.HD, (double) n1 / 2 * N2 / n2));
            collector.accept(createMetric(MetricType.HL, (double) 2 / n1 * n2 / N2));
            collector.accept(createMetric(MetricType.HEF, (double) (n1 * N2 * (N1 + N2) * Math.log(n1 + n2)) / (2 * n2)));
            collector.accept(createMetric(MetricType.HVC, (double) (N1 + N2) * Math.log(n1 + n2) / Math.log(2)));
            collector.accept(createMetric(MetricType.HER, (double) ((n1 * N2 * (N1 + N2) * Math.log(n1 + n2)) / (2 * n2)) / 3000));
        }
    }

    private void addOperator(String operator) {
        operators.add(operator);
        operatorList.add(operator);
    }

    private void addOperand(String operand) {
        operands.add(operand);
        operandList.add(operand);
    }

    // General expressions
    @Override
    public void visit(NameExpr n, Consumer<Metric> collector) {
        addOperand(n.getNameAsString());
        super.visit(n, collector);
    }

    @Override
    public void visit(MethodCallExpr n, Consumer<Metric> collector) {
        addOperand(n.getNameAsString());
        addOperator("()");
        super.visit(n, collector);
    }

    @Override
    public void visit(BinaryExpr n, Consumer<Metric> collector) {
        addOperator(n.getOperator().asString());
        super.visit(n, collector);
    }

    @Override
    public void visit(UnaryExpr n, Consumer<Metric> collector) {
        addOperator(n.getOperator().asString());
        super.visit(n, collector);
    }

    @Override
    public void visit(AssignExpr n, Consumer<Metric> collector) {
        addOperator(n.getOperator().asString());
        super.visit(n, collector);
    }

    @Override
    public void visit(ConditionalExpr n, Consumer<Metric> collector) {
        addOperator("?");
        addOperator(":");
        super.visit(n, collector);
    }

    // Literals
    @Override
    public void visit(StringLiteralExpr n, Consumer<Metric> collector) {
        addOperand(n.getValue());
        super.visit(n, collector);
    }

    @Override
    public void visit(IntegerLiteralExpr n, Consumer<Metric> collector) {
        addOperand(n.getValue());
        super.visit(n, collector);
    }

    @Override
    public void visit(DoubleLiteralExpr n, Consumer<Metric> collector) {
        addOperand(n.getValue());
        super.visit(n, collector);
    }

    @Override
    public void visit(BooleanLiteralExpr n, Consumer<Metric> collector) {
        addOperand(String.valueOf(n.getValue()));
        super.visit(n, collector);
    }

    @Override
    public void visit(CharLiteralExpr n, Consumer<Metric> collector) {
        addOperand(n.getValue());
        super.visit(n, collector);
    }

    @Override
    public void visit(NullLiteralExpr n, Consumer<Metric> collector) {
        addOperand("null");
        super.visit(n, collector);
    }

    // Statements
    @Override
    public void visit(IfStmt n, Consumer<Metric> collector) {
        addOperator("if");
        super.visit(n, collector);
    }

    @Override
    public void visit(ForStmt n, Consumer<Metric> collector) {
        addOperator("for");
        super.visit(n, collector);
    }

    @Override
    public void visit(ForEachStmt n, Consumer<Metric> collector) {
        addOperator("for");
        super.visit(n, collector);
    }

    @Override
    public void visit(WhileStmt n, Consumer<Metric> collector) {
        addOperator("while");
        super.visit(n, collector);
    }

    @Override
    public void visit(DoStmt n, Consumer<Metric> collector) {
        addOperator("do");
        addOperator("while");
        super.visit(n, collector);
    }

    @Override
    public void visit(SwitchStmt n, Consumer<Metric> collector) {
        addOperator("switch");
        super.visit(n, collector);
    }

    @Override
    public void visit(SwitchEntry n, Consumer<Metric> collector) {
        if (n.getLabels().isEmpty()) {
            addOperator("default");
        } else {
            n.getLabels().forEach(l -> addOperator("case"));
        }
        super.visit(n, collector);
    }

    @Override
    public void visit(ReturnStmt n, Consumer<Metric> collector) {
        addOperator("return");
        super.visit(n, collector);
    }

    @Override
    public void visit(ThrowStmt n, Consumer<Metric> collector) {
        addOperator("throw");
        super.visit(n, collector);
    }

    @Override
    public void visit(BreakStmt n, Consumer<Metric> collector) {
        addOperator("break");
        super.visit(n, collector);
    }

    @Override
    public void visit(ContinueStmt n, Consumer<Metric> collector) {
        addOperator("continue");
        super.visit(n, collector);
    }

    @Override
    public void visit(TryStmt n, Consumer<Metric> collector) {
        addOperator("try");
        super.visit(n, collector);
    }

    @Override
    public void visit(CatchClause n, Consumer<Metric> collector) {
        addOperator("catch");
        super.visit(n, collector);
    }

    // Types
    @Override
    public void visit(PrimitiveType n, Consumer<Metric> collector) {
        addOperator(n.asString());
        super.visit(n, collector);
    }

    @Override
    public void visit(VoidType n, Consumer<Metric> collector) {
        addOperator(n.asString());
        super.visit(n, collector);
    }

    @Override
    public void visit(ClassOrInterfaceType n, Consumer<Metric> collector) {
        addOperator(n.getNameAsString());
        super.visit(n, collector);
    }

    private Metric createMetric(MetricType type, double value) {
        return Metric.of(type, Value.of(value));
    }
}
