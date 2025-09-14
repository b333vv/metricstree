package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.CCM;

/**
 * Kotlin Cognitive Complexity (CCM) - simplified Campbell rules for Kotlin PSI.
 * Heuristics:
 * - +1 for each decision structure: if, when, while, do-while, for, catch.
 * - +nesting for being nested inside another decision (tracked by depth).
 * - +1 for boolean operator transitions (&&, ||) chains.
 * - +1 for recursion (call to function with same name and arity).
 *
 * This is a pragmatic approximation consistent with the existing Java implementation style.
 */
public class KotlinCognitiveComplexityVisitor extends KotlinMethodVisitor {

    private int complexity;
    private int nesting;

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        complexity = 0;
        nesting = 0;
        KtExpression body = function.getBodyExpression();
        if (body != null) {
            body.accept(new BodyVisitor(function));
        }
        metric = Metric.of(CCM, complexity);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        complexity = 0;
        nesting = 0;
        KtExpression body = constructor.getBodyExpression();
        if (body != null) {
            body.accept(new BodyVisitor(null));
        }
        metric = Metric.of(CCM, complexity);
    }

    private class BodyVisitor extends KtTreeVisitorVoid {
        private final KtNamedFunction owner;
        BodyVisitor(KtNamedFunction owner) { this.owner = owner; }

        @Override
        public void visitIfExpression(@NotNull KtIfExpression expression) {
            enterDecision();
            super.visitIfExpression(expression);
            exitDecision();
        }

        @Override
        public void visitWhenExpression(@NotNull KtWhenExpression expression) {
            enterDecision();
            super.visitWhenExpression(expression);
            exitDecision();
        }

        @Override
        public void visitForExpression(@NotNull KtForExpression expression) {
            enterDecision();
            super.visitForExpression(expression);
            exitDecision();
        }

        @Override
        public void visitWhileExpression(@NotNull KtWhileExpression expression) {
            enterDecision();
            super.visitWhileExpression(expression);
            exitDecision();
        }

        @Override
        public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
            enterDecision();
            super.visitDoWhileExpression(expression);
            exitDecision();
        }

        @Override
        public void visitTryExpression(@NotNull KtTryExpression expression) {
            // count catch sections
            for (KtCatchClause ignored : expression.getCatchClauses()) {
                enterDecision();
                // visit inside catch
                if (ignored.getCatchBody() != null) ignored.getCatchBody().accept(this);
                exitDecision();
            }
            super.visitTryExpression(expression);
        }

        @Override
        public void visitBinaryExpression(@NotNull KtBinaryExpression expression) {
            // Count boolean operator transitions (&&, ||)
            if (expression.getOperationToken() == KtTokens.ANDAND || expression.getOperationToken() == KtTokens.OROR) {
                complexity += 1;
            }
            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitCallExpression(@NotNull KtCallExpression expression) {
            // Simple recursion check: callee name equals owner name and same arity
            if (owner != null && expression.getCalleeExpression() != null) {
                String callee = expression.getCalleeExpression().getText();
                if (owner.getName() != null && owner.getName().equals(callee)) {
                    int args = expression.getValueArguments().size();
                    int params = owner.getValueParameters().size();
                    if (args == params) {
                        complexity += 1;
                    }
                }
            }
            super.visitCallExpression(expression);
        }

        private void enterDecision() { complexity += 1 + nesting; nesting++; }
        private void exitDecision() { nesting = Math.max(0, nesting - 1); }
    }
}
