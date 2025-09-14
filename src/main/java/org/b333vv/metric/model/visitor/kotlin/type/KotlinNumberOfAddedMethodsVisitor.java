package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOAM;

/**
 * Kotlin Number Of Added Methods (NOAM)
 * Heuristic: count non-constructor functions declared in class body that are not overrides,
 * plus private functions (which are considered "added"). Kotlin has no static methods, so
 * we ignore that aspect.
 */
public class KotlinNumberOfAddedMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int added = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) d;
                    boolean isOverride = f.hasModifier(KtTokens.OVERRIDE_KEYWORD);
                    boolean isPrivate = f.hasModifier(KtTokens.PRIVATE_KEYWORD);
                    if (isPrivate || !isOverride) {
                        added++;
                    }
                }
            }
        }
        metric = Metric.of(NOAM, added);
    }
}
