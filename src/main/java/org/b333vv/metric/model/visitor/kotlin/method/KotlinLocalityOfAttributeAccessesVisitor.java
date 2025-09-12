/*
 * Kotlin Locality Of Attribute Accesses (LAA) - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.LAA;

/**
 * Heuristic implementation for LAA on Kotlin PSI.
 * Own accesses: references to properties declared in the same Kotlin class, including implicit `this`.
 * Foreign accesses: qualified property accesses where the receiver is not `this`/`super`.
 */
public class KotlinLocalityOfAttributeAccessesVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        compute(function, function.getBodyExpression());
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        compute(constructor, constructor.getBodyExpression());
    }

    private void compute(@NotNull KtElement context, KtExpression body) {
        Set<String> ownPropertyNames = collectOwnPropertyNames(context);
        if (body == null) {
            metric = Metric.of(LAA, 0.0);
            return;
        }
        final int[] own = {0};
        final int[] total = {0};
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitDotQualifiedExpression(@NotNull KtDotQualifiedExpression expression) {
                // Count only property access a.b (selector is a simple name) and not a.b()
                KtExpression selector = expression.getSelectorExpression();
                if (selector instanceof KtSimpleNameExpression) {
                    total[0] += 1;
                    KtExpression receiver = expression.getReceiverExpression();
                    if (receiver instanceof KtThisExpression || receiver instanceof KtSuperExpression) {
                        own[0] += 1;
                    }
                }
                super.visitDotQualifiedExpression(expression);
            }

            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                // If this simple name is the selector of a qualified expression, it will be accounted by qualified handler
                if (expression.getParent() instanceof KtDotQualifiedExpression) {
                    KtDotQualifiedExpression dq = (KtDotQualifiedExpression) expression.getParent();
                    if (dq.getSelectorExpression() == expression) {
                        super.visitSimpleNameExpression(expression);
                        return;
                    }
                }
                if (expression.getParent() instanceof KtSafeQualifiedExpression) {
                    KtSafeQualifiedExpression sq = (KtSafeQualifiedExpression) expression.getParent();
                    if (sq.getSelectorExpression() == expression) {
                        super.visitSimpleNameExpression(expression);
                        return;
                    }
                }
                String name = expression.getReferencedName();
                if (ownPropertyNames.contains(name)) {
                    own[0] += 1;
                    total[0] += 1;
                }
                super.visitSimpleNameExpression(expression);
            }
        });
        if (total[0] == 0) {
            metric = Metric.of(LAA, 0.0);
        } else {
            metric = Metric.of(LAA, ((double) own[0]) / ((double) total[0]));
        }
    }

    private Set<String> collectOwnPropertyNames(@NotNull KtElement element) {
        Set<String> names = new HashSet<>();
        KtClassOrObject owner = findOwnerClass(element);
        if (owner != null) {
            for (KtDeclaration decl : owner.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    String n = ((KtProperty) decl).getName();
                    if (n != null) names.add(n);
                }
            }
            if (owner instanceof KtClass) {
                KtPrimaryConstructor ctor = ((KtClass) owner).getPrimaryConstructor();
                if (ctor != null) {
                    for (KtParameter p : ctor.getValueParameters()) {
                        if (p.hasValOrVar()) {
                            String n = p.getName();
                            if (n != null) names.add(n);
                        }
                    }
                }
            }
        }
        return names;
    }

    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        KtElement e = element;
        while (e != null && !(e instanceof KtClassOrObject)) {
            e = (KtElement) e.getParent();
        }
        return (KtClassOrObject) e;
    }
}
