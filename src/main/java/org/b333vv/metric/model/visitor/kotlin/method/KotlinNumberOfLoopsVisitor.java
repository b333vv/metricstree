package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOL;

/**
 * Kotlin Number Of Loops (NOL) - counts for, while, do-while loops inside a method/constructor body.
 */
public class KotlinNumberOfLoopsVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(NOL, countLoops(function.getBodyExpression()));
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(NOL, countLoops(constructor.getBodyExpression()));
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        metric = Metric.of(NOL, 0);
    }

    private long countLoops(KtExpression body) {
        if (body == null) return 0;
        final long[] c = {0};
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitForExpression(@NotNull KtForExpression expression) {
                c[0]++;
                super.visitForExpression(expression);
            }

            @Override
            public void visitWhileExpression(@NotNull KtWhileExpression expression) {
                c[0]++;
                super.visitWhileExpression(expression);
            }

            @Override
            public void visitDoWhileExpression(@NotNull KtDoWhileExpression expression) {
                c[0]++;
                super.visitDoWhileExpression(expression);
            }
        });
        return c[0];
    }
}
