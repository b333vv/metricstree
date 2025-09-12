/*
 * Kotlin Condition Nesting Depth - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.CND;

/**
 * Computes maximum nesting depth of conditional constructs for Kotlin functions:
 * - if (including else-if chains as depth increments when nested in branches)
 * - when (nested when inside branches increases depth)
 */
public class KotlinConditionNestingDepthVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int depth = maxConditionalDepth(function.getBodyExpression());
        metric = Metric.of(CND, depth);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int depth = maxConditionalDepth(constructor.getBodyExpression());
        metric = Metric.of(CND, depth);
    }

    private int maxConditionalDepth(KtExpression body) {
        if (body == null) return 0;
        final int[] max = {0};
        body.accept(new KtTreeVisitorVoid() {
            private int current = 0;

            @Override
            public void visitIfExpression(@NotNull KtIfExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitIfExpression(expression);
                current--;
            }

            @Override
            public void visitWhenExpression(@NotNull KtWhenExpression expression) {
                current++;
                max[0] = Math.max(max[0], current);
                super.visitWhenExpression(expression);
                current--;
            }
        });
        return max[0];
    }
}
