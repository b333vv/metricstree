/*
 * Kotlin Foreign Data Providers (FDP) - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.FDP;

/**
 * Counts distinct foreign providers whose properties are accessed within a function.
 * Heuristics due to limited resolution:
 * - Each qualified access receiver (a.b or a?.b) counts as a provider key by its textual receiver expression.
 * - Implicit and explicit this/super are treated as local and excluded.
 * - Unqualified name references are treated as local and do not contribute.
 */
public class KotlinForeignDataProvidersVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        compute(function.getBodyExpression());
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        compute(constructor.getBodyExpression());
    }

    private void compute(KtExpression body) {
        if (body == null) {
            metric = Metric.of(FDP, 0);
            return;
        }
        final Set<String> providers = new HashSet<>();
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                KtExpression receiver = expression.getReceiverExpression();
                String key = normalizeReceiverKey(receiver);
                if (key != null) providers.add(key);
                super.visitDotQualifiedExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                KtExpression receiver = expression.getReceiverExpression();
                String key = normalizeReceiverKey(receiver);
                if (key != null) providers.add(key);
                super.visitSafeQualifiedExpression(expression);
            }

            private String normalizeReceiverKey(KtExpression receiver) {
                if (receiver == null) return null;
                if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) return null;
                String text = receiver.getText();
                if (text == null) return null;
                text = text.trim();
                if (text.isEmpty() || "this".equals(text) || "super".equals(text)) return null;
                return text;
            }
        });
        metric = Metric.of(FDP, providers.size());
    }
}
