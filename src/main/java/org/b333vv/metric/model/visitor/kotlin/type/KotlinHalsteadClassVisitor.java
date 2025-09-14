package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CHEF;
import static org.b333vv.metric.model.metric.MetricType.CHER;
import static org.b333vv.metric.model.metric.MetricType.CHL;
import static org.b333vv.metric.model.metric.MetricType.CHD;
import static org.b333vv.metric.model.metric.MetricType.CHVC;
import static org.b333vv.metric.model.metric.MetricType.CHVL;

/**
 * Kotlin Halstead metrics for classes using PSI-based heuristics.
 * We count operands and operators by visiting Kotlin expressions, names, and literals.
 */
public class KotlinHalsteadClassVisitor extends KotlinClassVisitor {

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
    public void visitClass(@NotNull KtClass klass) {
        numberOfOperands = 0;
        numberOfOperators = 0;
        operators.clear();
        operands.clear();

        // Visit class body expressions
        KtClassBody body = klass.getBody();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                    KtOperationReferenceExpression opRef = expression.getOperationReference();
                    String op = opRef.getText();
                    registerOperator(op);
                    super.visitBinaryExpression(expression);
                }

                @Override
                public void visitPrefixExpression(@NotNull KtPrefixExpression expression) {
                    String op = expression.getOperationReference().getText();
                    registerOperator(op);
                    super.visitPrefixExpression(expression);
                }

                @Override
                public void visitPostfixExpression(@NotNull KtPostfixExpression expression) {
                    String op = expression.getOperationReference().getText();
                    registerOperator(op);
                    super.visitPostfixExpression(expression);
                }

                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    // Treat function name as operator signature
                    KtExpression callee = expression.getCalleeExpression();
                    if (callee != null) {
                        registerOperator(callee.getText());
                    }
                    super.visitCallExpression(expression);
                }

                @Override
                public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                    // Treat identifiers as operands
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

        // Emit metrics into metrics list via metric field sequentially
        // This class mirrors Java HalsteadClassVisitor, but Kotlin pipeline collects single metrics,
        // so we sequentially assign and the caller will read via KotlinModelBuilder.
        // Instead, let KotlinModelBuilder instantiate and add all 6 metrics after compute.
        metric = Metric.of(CHVL, volume());
    }

    public java.util.List<Metric> buildMetrics() {
        java.util.List<Metric> list = new LinkedList<>();
        list.add(Metric.of(CHEF, effort()));
        list.add(Metric.of(CHER, errors()));
        list.add(Metric.of(CHVL, volume()));
        list.add(Metric.of(CHD, difficulty()));
        list.add(Metric.of(CHVC, vocabulary()));
        list.add(Metric.of(CHL, length()));
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
