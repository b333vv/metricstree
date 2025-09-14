package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOOM;

/**
 * Kotlin Number Of Overridden Methods (NOOM)
 * Counts functions declared in the class body that have the 'override' modifier.
 */
public class KotlinNumberOfOverriddenMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int overridden = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) d;
                    if (f.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                        overridden++;
                    }
                }
            }
        }
        metric = Metric.of(NOOM, overridden);
    }
}
