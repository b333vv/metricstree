/*
 * Kotlin Number Of Children (NOC) - optimized for Kotlin's final-by-default semantics
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;

import static org.b333vv.metric.model.metric.MetricType.NOC;

/**
 * NOC for Kotlin classes.
 * In Kotlin, classes are final by default and cannot be inherited unless marked
 * as open, abstract, or sealed.
 * This visitor optimizes by returning 0 immediately for final classes.
 * For open/abstract/sealed classes, proper implementation would require
 * project-wide inheritor search.
 */
public class KotlinNumberOfChildrenVisitor extends KotlinClassVisitor {
    @Override
    public void visitClass(@NotNull KtClass klass) {
        // Kotlin classes are final by default - they cannot have children unless
        // open/abstract/sealed
        if (!KotlinMetricUtils.isOpenOrAbstract(klass)) {
            metric = Metric.of(NOC, 0);
            return;
        }

        // For open/abstract/sealed classes, proper implementation requires inheritor
        // search
        // For now, return 0 as placeholder (Phase 3 can implement proper search)
        metric = Metric.of(NOC, 0);
    }
}
