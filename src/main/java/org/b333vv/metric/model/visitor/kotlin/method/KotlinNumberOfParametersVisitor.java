/*
 * Kotlin Number Of Parameters - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;
import org.jetbrains.kotlin.psi.KtSecondaryConstructor;

import static org.b333vv.metric.model.metric.MetricType.NOPM;

public class KotlinNumberOfParametersVisitor extends KotlinMethodVisitor {
    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        int count = function.getValueParameters().size();
        metric = Metric.of(NOPM, count);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        int count = constructor.getValueParameters().size();
        metric = Metric.of(NOPM, count);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        int count = constructor.getValueParameters().size();
        metric = Metric.of(NOPM, count);
    }
}
