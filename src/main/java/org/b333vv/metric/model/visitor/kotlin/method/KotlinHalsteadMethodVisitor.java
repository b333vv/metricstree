package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.*;

/**
 * Kotlin Halstead metrics for methods using PSI-based heuristics.
 */
public class KotlinHalsteadMethodVisitor extends KotlinMethodVisitor {

    private int numberOfOperands;
    private int numberOfOperators;
    private final Set<String> operators = new HashSet<>();
    private final Set<String> operands = new HashSet<>();

    private int distinctOperands() { return operands.size(); }
    private int distinctOperators() { return operators.size(); }

    private long length() { return numberOfOperands + numberOfOperators; }
    private long vocabulary() { return (long) operands.size() + (long) operators.size(); }

    private double difficulty() {
        final int N2 = numberOfOperands;
        final int n1 = distinctOperators();
        final int n2 = distinctOperands();
        return n2 == 0 ? 0.0 : ((double) n1 / 2.0) * ((double) N2 / (double) n2);
    }

    private double volume() { return (double) length() * Math.log(Math.max(1, vocabulary())) / Math.log(2.0); }
    private double effort() { return difficulty() * volume(); }
    private double errors() { return Math.pow(effort(), 2.0 / 3.0) / 3000.0; }

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        compute(function.getBodyExpression());
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // No body block, skip
        metric = Metric.of(HVL, 0);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        compute(constructor.getBodyExpression());
    }

    private void compute(KtExpression body) {
        numberOfOperands = 0;
        numberOfOperators = 0;
        operators.clear();
        operands.clear();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                    registerOperator(expression.getOperationReference().getText());
                    super.visitBinaryExpression(expression);
                }

                @Override
                public void visitPrefixExpression(@NotNull KtPrefixExpression expression) {
                    registerOperator(expression.getOperationReference().getText());
                    super.visitPrefixExpression(expression);
                }

                @Override
                public void visitPostfixExpression(@NotNull KtPostfixExpression expression) {
                    registerOperator(expression.getOperationReference().getText());
                    super.visitPostfixExpression(expression);
                }

                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    KtExpression callee = expression.getCalleeExpression();
                    if (callee != null) registerOperator(callee.getText());
                    super.visitCallExpression(expression);
                }

                @Override
                public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                    String text = expression.getText();
                    if (text != null && !text.isEmpty()) registerOperand(text);
                    super.visitSimpleNameExpression(expression);
                }

                @Override
                public void visitStringTemplateExpression(@NotNull KtStringTemplateExpression expression) {
                    registerOperand(expression.getText());
                    super.visitStringTemplateExpression(expression);
                }

                @Override
                public void visitConstantExpression(@NotNull KtConstantExpression expression) {
                    registerOperand(expression.getText());
                    super.visitConstantExpression(expression);
                }
            });
        }
        // Store primary metric (HVL) in metric field for compatibility
        metric = Metric.of(HVL, volume());
    }

    public java.util.List<Metric> buildMetrics() {
        java.util.List<Metric> list = new LinkedList<>();
        list.add(Metric.of(HEF, effort()));
        list.add(Metric.of(HER, errors()));
        list.add(Metric.of(HVL, volume()));
        list.add(Metric.of(HD, difficulty()));
        list.add(Metric.of(HVC, vocabulary()));
        list.add(Metric.of(HL, length()));
        return list;
    }

    private void registerOperator(String operator) {
        if (operator == null) return;
        numberOfOperators++;
        operators.add(operator);
    }

    private void registerOperand(String operand) {
        if (operand == null) return;
        numberOfOperands++;
        operands.add(operand);
    }
}
