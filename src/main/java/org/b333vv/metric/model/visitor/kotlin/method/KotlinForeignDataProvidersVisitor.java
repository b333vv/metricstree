/*
 * Kotlin Foreign Data Providers (FDP) - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
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
        compute(function, function.getBodyExpression());
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        compute(constructor, constructor.getBodyExpression());
    }

    private void compute(@NotNull KtElement context, KtExpression body) {
        if (body == null) {
            metric = Metric.of(FDP, 0);
            return;
        }
        KtClassOrObject owner = findOwnerClass(context);
        final Set<String> providers = new HashSet<>();
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                String key = resolveProviderKey(expression.getSelectorExpression(), expression.getReceiverExpression(), owner);
                if (key != null) providers.add(key);
                super.visitDotQualifiedExpression(expression);
            }

            @Override
            public void visitSafeQualifiedExpression(@NotNull KtSafeQualifiedExpression expression) {
                String key = resolveProviderKey(expression.getSelectorExpression(), expression.getReceiverExpression(), owner);
                if (key != null) providers.add(key);
                super.visitSafeQualifiedExpression(expression);
            }

            private String resolveProviderKey(KtExpression selector, KtExpression receiver, KtClassOrObject owner) {
                // Prefer resolving selector to a property and using its owning class FQN
                if (selector instanceof KtSimpleNameExpression) {
                    for (var ref : selector.getReferences()) {
                        PsiElement resolved = ref.resolve();
                        if (resolved instanceof PsiField) {
                            PsiField f = (PsiField) resolved;
                            if (f.getContainingClass() != null) {
                                String qn = f.getContainingClass().getQualifiedName();
                                if (qn != null && !isOwnerFqn(owner, qn)) return qn;
                            }
                        }
                        if (resolved instanceof KtProperty) {
                            KtClassOrObject declOwner = findOwnerClass((KtProperty) resolved);
                            if (declOwner != null && declOwner.getFqName() != null) {
                                String qn = declOwner.getFqName().asString();
                                if (!isOwnerFqn(owner, qn)) return qn;
                            }
                        }
                    }
                }
                // Fallback to receiver textual key
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

    private boolean isOwnerFqn(KtClassOrObject owner, String fqn) {
        return owner != null && owner.getFqName() != null && fqn.equals(owner.getFqName().asString());
    }

    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        PsiElement e = element;
        while (e != null && !(e instanceof KtClassOrObject)) {
            e = e.getParent();
            if (e != null && !(e instanceof KtElement)) {
                break;
            }
        }
        return (e instanceof KtClassOrObject) ? (KtClassOrObject) e : null;
    }

    private KtClassOrObject findOwnerClass(@NotNull KtProperty property) {
        return findOwnerClass((KtElement) property);
    }
}
