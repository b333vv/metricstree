/*
 * Kotlin McCabe Cyclomatic Complexity - Phase 2.3.1
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.CC;

/**
 * Computes cyclomatic complexity for Kotlin functions and constructors.
 * Rules (initial mapping):
 *  - if
 *  - when (each entry counts as 1; else entry also counts)
 *  - for, while, do-while
 *  - catch
 *  - boolean operators && and || (each occurrence)
 *  - elvis operator ?: when the right side is a return or throw
 *  - safe calls ?. when used in a conditional context (heuristic: inside if/when condition)
 */
public class KotlinMcCabeCyclomaticComplexityVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int complexity = 1; // baseline
        complexity += computeForBody(function.getBodyExpression());
        metric = Metric.of(CC, complexity);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // constructors can have init blocks in class; for simplicity, treat as 1
        metric = Metric.of(CC, 1);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int complexity = 1;
        KtBlockExpression body = constructor.getBodyExpression();
        complexity += computeForBody(body);
        metric = Metric.of(CC, complexity);
    }

    private int computeForBody(KtExpression body) {
        if (body == null) return 0;
        final int[] c = {0};
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitIfExpression(@NotNull KtIfExpression expression) {
                c[0] += 1;
                super.visitIfExpression(expression);
            }

            @Override
            public void visitWhenExpression(@NotNull KtWhenExpression expression) {
                // count each entry
                c[0] += Math.max(1, expression.getEntries().size());
                super.visitWhenExpression(expression);
            }

            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                c[0] += 1;
                super.visitForExpression(expression);
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                c[0] += 1;
                super.visitWhileExpression(expression);
            }

            @Override
            public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                c[0] += 1;
                super.visitDoWhileExpression(expression);
            }

            @Override
            public void visitTryExpression(@NotNull KtTryExpression expression) {
                // each catch increases complexity
                c[0] += Math.max(0, expression.getCatchClauses().size());
                super.visitTryExpression(expression);
            }

            @Override
            public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
                if (expression.getOperationToken() == KtTokens.ANDAND || expression.getOperationToken() == KtTokens.OROR) {
                    c[0] += 1;
                }
                if (expression.getOperationToken() == KtTokens.ELVIS) {
                    // Heuristic: count when right side is a return or throw expression
                    KtExpression right = expression.getRight();
                    if (right instanceof KtReturnExpression || right instanceof KtThrowExpression) {
                        c[0] += 1;
                    }
                }
                super.visitBinaryExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                // Heuristic: count safe call as branch when used in condition
                if (isInCondition(expression)) {
                    c[0] += 1;
                }
                super.visitSafeQualifiedExpression(expression);
            }

            private boolean isInCondition(@NotNull KtExpression expr) {
                Object parent = expr.getParent();
                if (parent instanceof KtIfExpression) {
                    return ((KtIfExpression) parent).getCondition() == expr;
                }
                if (parent instanceof KtWhenCondition) {
                    return true;
                }
                return false;
            }
        });
        return c[0];
    }
}
