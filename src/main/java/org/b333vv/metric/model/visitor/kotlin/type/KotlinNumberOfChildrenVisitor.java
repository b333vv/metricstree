/*
 * Kotlin Number Of Children (NOC) - placeholder heuristic
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;

import static org.b333vv.metric.model.metric.MetricType.NOC;

/**
 * NOC requires project-wide search for inheritors which is not trivial on PSI without resolve.
 * For Phase 2 scaffolding, we provide a placeholder returning 0 for non-final classes and UNDEFINED otherwise.
 * Phase 3 can replace with proper search via indices or Kotlin light classes.
 */
public class KotlinNumberOfChildrenVisitor extends KotlinClassVisitor {
    @Override
    public void visitClass(@NotNull KtClass klass) {
        // If the class is a Kotlin interface or is final (default open semantics differ), we still return 0 here.
        // Proper implementation will use inheritors search.
        metric = Metric.of(NOC, 0);
    }
}
