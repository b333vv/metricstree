package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.MND;

/**
 * Kotlin Maximum Nesting Depth (MND) - counts the deepest nesting level of control structures
 * like if/when/for/while/do-while/try-catch within a method.
 */
public class KotlinMaximumNestingDepthVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(MND, maxDepth(function.getBodyExpression()));
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(MND, maxDepth(constructor.getBodyExpression()));
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        metric = Metric.of(MND, 0);
    }

    private int maxDepth(KtExpression body) {
        if (body == null) return 0;
        final int[] max = {0};
        traverse(body, 0, max);
        return max[0];
    }

    private void traverse(KtElement element, int depth, int[] max) {
        if (element == null) return;
        if (element instanceof KtIfExpression ||
                element instanceof KtWhenExpression ||
                element instanceof KtForExpression ||
                element instanceof KtWhileExpression ||
                element instanceof KtDoWhileExpression ||
                element instanceof KtTryExpression) {
            depth++;
            if (depth > max[0]) max[0] = depth;
        }
        final int currentDepth = depth;
        element.acceptChildren(new KtTreeVisitorVoid() {
            @Override
            public void visitKtElement(@NotNull KtElement element) {
                traverse(element, currentDepth, max);
                super.visitKtElement(element);
            }
        });
    }
}
