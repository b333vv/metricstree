/*
 * Kotlin Number Of Methods (NOM) - initial implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOM;

/**
 * Counts methods for a Kotlin class: named functions declared in the class body
 * plus primary constructor (if present) and all secondary constructors.
 *
 * Notes:
 * - Companion/nested object functions are not counted towards the enclosing class NOM in this initial version,
 *   mirroring that Java static members belong to the class body itself, not nested types.
 */
public class KotlinNumberOfMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int count = 0;

        // Primary constructor counts as a method
        if (klass.getPrimaryConstructor() != null) {
            count += 1;
        }
        // Secondary constructors
        count += klass.getSecondaryConstructors().size();

        // Named functions in the class body
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    count += 1;
                }
            }
        }

        metric = Metric.of(NOM, count);
    }
}
