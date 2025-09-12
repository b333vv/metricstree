/*
 * Kotlin Non-Commenting Source Statements (NCSS) - initial implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NCSS;

/**
 * Counts non-commenting source statements for a Kotlin class.
 * Heuristics adapted from Java implementation to Kotlin PSI.
 *
 * What we count:
 *  - Class declaration itself
 *  - Each property declaration
 *  - Each function/constructor declaration
 *  - Control-flow and executable statements inside bodies:
 *      for/while/do-while, when entries, return/throw/break/continue,
 *      assignment expressions, top-level call expressions in blocks
 *  - Else branch (if present) as a separate statement
 *  - Each catch and finally in try-expression
 */
public class KotlinNonCommentingSourceStatementsVisitor extends KotlinClassVisitor {
    private int statements = 0;

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int prev = statements;
        // Count the class declaration itself
        statements += 1;

        super.visitClass(klass);

        metric = Metric.of(NCSS, statements);
        statements = prev;
    }

    @Override
    public void visitProperty(@NotNull KtProperty property) {
        // Count property declaration
        statements += 1;
        super.visitProperty(property);
    }

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        // Count function declaration
        statements += 1;
        super.visitNamedFunction(function);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        // Count constructor declaration
        statements += 1;
        super.visitPrimaryConstructor(constructor);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        // Count constructor declaration
        statements += 1;
        super.visitSecondaryConstructor(constructor);
    }

    @Override
    public void visitIfExpression(@NotNull KtIfExpression expression) {
        super.visitIfExpression(expression);
        if (expression.getElse() != null) {
            // Count 'else' as extra statement
            statements += 1;
        }
    }

    @Override
    public void visitTryExpression(@NotNull KtTryExpression expression) {
        super.visitTryExpression(expression);
        // Count each catch and finally
        statements += Math.max(0, expression.getCatchClauses().size());
        if (expression.getFinallyBlock() != null) {
            statements += 1;
        }
    }

    @Override
    public void visitWhenExpression(@NotNull KtWhenExpression expression) {
        super.visitWhenExpression(expression);
        // Each entry (case/default) counts as a statement
        statements += expression.getEntries().size();
    }

    @Override
    public void visitForExpression(@NotNull KtForExpression expression) {
        statements += 1;
        super.visitForExpression(expression);
    }

    @Override
    public void visitWhileExpression(@NotNull KtWhileExpression expression) {
        statements += 1;
        super.visitWhileExpression(expression);
    }

    @Override
    public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
        statements += 1;
        super.visitDoWhileExpression(expression);
    }

    @Override
    public void visitReturnExpression(@NotNull KtReturnExpression expression) {
        statements += 1;
        super.visitReturnExpression(expression);
    }

    @Override
    public void visitThrowExpression(@NotNull KtThrowExpression expression) {
        statements += 1;
        super.visitThrowExpression(expression);
    }

    @Override
    public void visitBreakExpression(@NotNull KtBreakExpression expression) {
        statements += 1;
        super.visitBreakExpression(expression);
    }

    @Override
    public void visitContinueExpression(@NotNull KtContinueExpression expression) {
        statements += 1;
        super.visitContinueExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
        // Count assignment expressions as statements
        if (KtTokens.ALL_ASSIGNMENTS.contains(expression.getOperationToken())) {
            statements += 1;
        }
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitCallExpression(@NotNull KtCallExpression expression) {
        // Count top-level call expressions in a block as statements
        if (expression.getParent() instanceof KtBlockExpression) {
            statements += 1;
        }
        super.visitCallExpression(expression);
    }
}
