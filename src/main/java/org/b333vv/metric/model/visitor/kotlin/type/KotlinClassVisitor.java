/*
 * Kotlin visitors base - Phase 2
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtVisitorVoid;

/**
 * Base class for Kotlin class-level visitors operating on Kotlin PSI (KtClass, KtObjectDeclaration, etc.).
 *
 * Mirrors the intent of {@code org.b333vv.metric.model.visitor.type.JavaClassVisitor},
 * but works with Kotlin PSI directly. Integration with the domain model will be wired in Phase 3.
 */
public abstract class KotlinClassVisitor extends KtVisitorVoid {
    protected Metric metric;

    /**
     * @return last calculated metric, if any
     */
    @Nullable
    public Metric getMetric() {
        return metric;
    }

    /**
     * Entry point to compute metric for a Kotlin class. Subclasses should override
     * {@link #visitClass(KtClass)} to perform calculation and assign {@link #metric}.
     */
    public void computeFor(KtClass ktClass) {
        if (ktClass != null) {
            ktClass.accept(this);
        }
    }
}
