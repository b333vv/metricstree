/*
 * Kotlin Depth Of Inheritance Tree (DIT) - heuristic PSI implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.DIT;

/**
 * Heuristic DIT for Kotlin without full resolve:
 *  - 0 if there are no super type list entries or only implicit Any
 *  - 1 if there is at least one explicit super type
 * This will be refined with resolve in a later phase.
 */
public class KotlinDepthOfInheritanceTreeVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int depth = 0;
        if (!klass.getSuperTypeListEntries().isEmpty()) {
            depth = 1;
        }
        metric = Metric.of(DIT, depth);
    }
}
