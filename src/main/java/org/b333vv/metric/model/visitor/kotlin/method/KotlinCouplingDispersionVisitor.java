package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CDISP;

/**
 * Kotlin Coupling Dispersion (CDISP)
 * Heuristic: ratio of the number of distinct foreign providers invoked to the number of distinct foreign method calls.
 * - Distinct providers approximated by the textual receiver of qualified calls.
 * - Only counts qualified calls whose receiver is not this/super.
 */
public class KotlinCouplingDispersionVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(CDISP, compute(function.getBodyExpression()));
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(CDISP, compute(constructor.getBodyExpression()));
    }

    private double compute(KtExpression body) {
        if (body == null) return 0.0;
        final Set<String> providers = new HashSet<>();
        final Set<String> usedMethods = new HashSet<>();
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitQualifiedExpression(@NotNull KtQualifiedExpression expression) {
                KtExpression selector = expression.getSelectorExpression();
                if (selector instanceof KtCallExpression) {
                    String callee = ((KtCallExpression) selector).getCalleeExpression() != null ? ((KtCallExpression) selector).getCalleeExpression().getText() : null;
                    KtExpression receiver = expression.getReceiverExpression();
                    String key = receiverKey(receiver);
                    if (callee != null && key != null) {
                        providers.add(key);
                        usedMethods.add(key + "#" + callee);
                    }
                }
                super.visitQualifiedExpression(expression);
            }
        });
        int used = usedMethods.size();
        if (used == 0) return 0.0;
        return Value.of((double) providers.size()).divide(Value.of((double) used)).doubleValue();
    }

    private String receiverKey(KtExpression receiver) {
        if (receiver == null) return null;
        if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) return null;
        String text = receiver.getText();
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty() || "this".equals(text) || "super".equals(text)) return null;
        return text;
    }

    // no owner resolution needed
}
