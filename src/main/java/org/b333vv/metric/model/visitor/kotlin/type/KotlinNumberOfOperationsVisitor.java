package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOO;

/**
 * Kotlin Number Of Operations (NOO) - counts declared functions of the class (excluding constructors).
 * Does not include inherited members due to PSI-only nature.
 */
public class KotlinNumberOfOperationsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    count++;
                }
            }
        }
        metric = Metric.of(NOO, count);
    }
}
