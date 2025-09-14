package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CINT;

/**
 * Kotlin Coupling Intensity (CINT)
 * Heuristic: count distinct foreign method calls within a function body.
 * - Qualified calls like a.foo(), a?.foo(), obj.bar() are counted as foreign when receiver is not this/super.
 * - Unqualified calls are treated as local and ignored (no resolution attempted).
 * - Distinctness by receiver textual key + callee name.
 */
public class KotlinCouplingIntensityVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(CINT, count(function, function.getBodyExpression()));
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        metric = Metric.of(CINT, count(constructor, constructor.getBodyExpression()));
    }

    private long count(@NotNull KtElement context, KtExpression body) {
        if (body == null) return 0;
        KtClassOrObject owner = findOwnerClass(context);
        final Set<String> used = new HashSet<>();
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitQualifiedExpression(@NotNull KtQualifiedExpression expression) {
                KtExpression selector = expression.getSelectorExpression();
                if (selector instanceof KtCallExpression) {
                    String callee = ((KtCallExpression) selector).getCalleeExpression() != null ? ((KtCallExpression) selector).getCalleeExpression().getText() : null;
                    KtExpression receiver = expression.getReceiverExpression();
                    String key = receiverKey(receiver, owner);
                    if (callee != null && key != null) used.add(key + "#" + callee);
                }
                super.visitQualifiedExpression(expression);
            }
        });
        return used.size();
    }

    private String receiverKey(KtExpression receiver, KtClassOrObject owner) {
        if (receiver == null) return null;
        if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) return null;
        String text = receiver.getText();
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty() || "this".equals(text) || "super".equals(text)) return null;
        return text;
    }

    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        com.intellij.psi.PsiElement e = element;
        while (e != null && !(e instanceof KtClassOrObject)) {
            e = e.getParent();
            if (e != null && !(e instanceof KtElement)) break;
        }
        return (e instanceof KtClassOrObject) ? (KtClassOrObject) e : null;
    }
}
