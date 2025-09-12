/*
 * Kotlin Number Of Attributes (NOA) - initial implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOA;

/**
 * Counts attributes (properties) for a Kotlin class.
 * Includes:
 *  - Properties declared in the primary constructor (val/var parameters)
 *  - Properties declared in the class body (KtProperty)
 * Excludes:
 *  - Properties inside companion/nested objects (treated as nested types)
 *  - Inherited properties (future enhancement)
 */
public class KotlinNumberOfAttributesVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;

        // Primary constructor properties (val/var parameters)
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter p : primary.getValueParameters()) {
                if (p.hasValOrVar()) {
                    count += 1;
                }
            }
        }

        // Class body properties (exclude nested objects)
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    count += 1;
                }
            }
        }

        metric = Metric.of(NOA, count);
    }
}
