/*
 * Kotlin Response For Class (RFC) - PSI-based implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.RFC;

/**
 * RFC for Kotlin: size of the set consisting of
 *  - methods declared in the class (functions and constructors)
 *  - plus unique method calls from within the class bodies (by simple name)
 *
 * This PSI-only approach uses call names without resolve; Phase 3 can augment with proper resolution.
 */
public class KotlinResponseForClassVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        try {
            Set<String> responses = new HashSet<>();

            // Declared methods: functions and constructors
            if (klass.getPrimaryConstructor() != null) {
                responses.add("<init>");
            }
            responses.addAll(klass.getSecondaryConstructors().stream().map(c -> "<init>").toList());

            KtClassBody body = klass.getBody();
            if (body != null) {
                for (KtDeclaration decl : body.getDeclarations()) {
                    if (decl instanceof KtNamedFunction) {
                        String sig = ((KtNamedFunction) decl).getName();
                        if (sig != null) responses.add(sig);
                    }
                }
            }

            // Traverse class to collect call expressions
            klass.accept(new KtTreeVisitorVoid() {
                @Override
                public void visitCallExpression(@NotNull KtCallExpression expression) {
                    KtExpression calleeExpr = expression.getCalleeExpression();
                    if (calleeExpr instanceof KtSimpleNameExpression) {
                        String name = ((KtSimpleNameExpression) calleeExpr).getReferencedName();
                        if (name != null && !name.isEmpty()) {
                            responses.add(name);
                        }
                    }
                    super.visitCallExpression(expression);
                }
            });

            metric = Metric.of(RFC, responses.size());
        } catch (Exception e) {
            metric = Metric.of(RFC, Value.UNDEFINED);
        }
    }
}
