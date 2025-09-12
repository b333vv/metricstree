/*
 * Kotlin Loop Nesting Depth - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.LND;

/**
 * Computes maximum nesting depth of loop constructs for Kotlin functions:
 * - for
 * - while
 * - do-while
 */
public class KotlinLoopNestingDepthVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int depth = maxLoopDepth(function.getBodyExpression());
        metric = Metric.of(LND, depth);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int depth = maxLoopDepth(constructor.getBodyExpression());
        metric = Metric.of(LND, depth);
    }

    private int maxLoopDepth(KtExpression body) {
        if (body == null) return 0;
        final int[] max = {0};
        body.accept(new KtTreeVisitorVoid() {
            private int current = 0;

            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitForExpression(expression);
                current--;
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitWhileExpression(expression);
                current--;
            }

            @Override
            public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitDoWhileExpression(expression);
                current--;
            }
        });
        return max[0];
    }
}
