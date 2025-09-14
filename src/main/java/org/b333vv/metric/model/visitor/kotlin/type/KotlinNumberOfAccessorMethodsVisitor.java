package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOAC;

/**
 * Kotlin Number Of Accessor Methods (NOAC)
 * Heuristic: count properties that declare a custom getter or setter body (i.e., not the implicit one).
 */
public class KotlinNumberOfAccessorMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtProperty) {
                    KtProperty p = (KtProperty) d;
                    KtPropertyAccessor getter = p.getGetter();
                    KtPropertyAccessor setter = p.getSetter();
                    if (getter != null && getter.getBodyExpression() != null) count++;
                    if (setter != null && setter.getBodyExpression() != null) count++;
                }
            }
        }
        metric = Metric.of(NOAC, count);
    }
}
