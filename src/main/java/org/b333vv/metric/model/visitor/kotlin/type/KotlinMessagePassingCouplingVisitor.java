package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.MPC;

/**
 * Kotlin Message Passing Coupling (MPC) - counts method call expressions inside the class body.
 * Includes calls in function bodies and initializers. Does not attempt resolution.
 */
public class KotlinMessagePassingCouplingVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        final int[] calls = {0};
        KtClassBody body = klass.getBody();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    calls[0]++;
                    super.visitCallExpression(expression);
                }
            });
        }
        metric = Metric.of(MPC, calls[0]);
    }
}
