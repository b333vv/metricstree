/*
 * Kotlin Number Of Accessed Variables (NOAV) - Phase 2.3.3
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.NOAV;

/**
 * Counts number of unique variables accessed within a function.
 * Heuristic without resolve:
 * - Track distinct simple name references that look like variables/properties.
 * - Exclude names used as function call callee.
 */
public class KotlinNumberOfAccessedVariablesVisitor extends KotlinMethodVisitor {

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        metric = Metric.of(NOAV, 0);
        KtExpression body = function.getBodyExpression();
        if (body == null) return;
        final Set<String> allowed = collectAllowedNames(function);
        final Set<String> names = new HashSet<>();
        body.accept(new KtTreeVisitorVoid() {
            @Override
            public void visitSimpleNameExpression(@NotNull KtSimpleNameExpression expression) {
                // Skip operator references like '+', '+=' which are represented as simple names in PSI
                if (expression instanceof KtOperationReferenceExpression) {
                    super.visitSimpleNameExpression(expression);
                    return;
                }
                if (!isFunctionCallee(expression)) {
                    String name = expression.getReferencedName();
                    if (name != null && !name.isEmpty() && allowed.contains(name)) {
                        names.add(name);
                    }
                }
                super.visitSimpleNameExpression(expression);
            }

            private boolean isFunctionCallee(@NotNull KtSimpleNameExpression expr) {
                if (expr.getParent() instanceof KtCallExpression) {
                    KtExpression callee = ((KtCallExpression) expr.getParent()).getCalleeExpression();
                    return callee == expr;
                }
                if (expr.getParent() instanceof KtDotQualifiedExpression) {
                    KtDotQualifiedExpression dq = (KtDotQualifiedExpression) expr.getParent();
                    if (dq.getSelectorExpression() == expr && dq.getParent() instanceof KtCallExpression) {
                        return true;
                    }
                }
                return false;
            }
        });
        metric = Metric.of(NOAV, names.size());
    }

    private Set<String> collectAllowedNames(@NotNull KtNamedFunction function) {
        Set<String> allowed = new HashSet<>();
        // Parameters
        function.getValueParameters().forEach(p -> {
            if (p.getName() != null) allowed.add(p.getName());
        });
        // Local properties declared in function body
        KtExpression body = function.getBodyExpression();
        if (body != null) {
            body.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitProperty(@NotNull KtProperty property) {
                    if (property.getName() != null) allowed.add(property.getName());
                    super.visitProperty(property);
                }
            });
        }
        // Owner class properties (including primary constructor val/var)
        KtClassOrObject owner = findOwnerClass(function);
        if (owner != null) {
            for (KtDeclaration decl : owner.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    String n = ((KtProperty) decl).getName();
                    if (n != null) allowed.add(n);
                }
            }
            if (owner instanceof KtClass) {
                KtPrimaryConstructor ctor = ((KtClass) owner).getPrimaryConstructor();
                if (ctor != null) {
                    for (KtParameter p : ctor.getValueParameters()) {
                        if (p.hasValOrVar()) {
                            String n = p.getName();
                            if (n != null) allowed.add(n);
                        }
                    }
                }
            }
        }
        return allowed;
    }

    private KtClassOrObject findOwnerClass(@NotNull KtElement element) {
        KtElement e = element;
        while (e != null && !(e instanceof KtClassOrObject)) {
            e = (KtElement) e.getParent();
        }
        return (KtClassOrObject) e;
    }
}
