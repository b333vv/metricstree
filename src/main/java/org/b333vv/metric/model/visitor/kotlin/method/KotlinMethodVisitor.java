/*
 * Kotlin visitors base - Phase 2
 */
package org.b333vv.metric.model.visitor.kotlin.method;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtPrimaryConstructor;
import org.jetbrains.kotlin.psi.KtSecondaryConstructor;
import org.jetbrains.kotlin.psi.KtVisitorVoid;

/**
 * Base class for Kotlin method-level visitors operating on Kotlin PSI (KtNamedFunction, constructors).
 * Mirrors the intent of {@code org.b333vv.metric.model.visitor.method.JavaMethodVisitor}.
 */
public abstract class KotlinMethodVisitor extends KtVisitorVoid {
    protected Metric metric;

    @Nullable
    public Metric getMetric() {
        return metric;
    }

    public void computeFor(KtNamedFunction function) {
        if (function != null) {
            function.accept(this);
        }
    }

    public void computeFor(KtPrimaryConstructor ctor) {
        if (ctor != null) {
            ctor.accept(this);
        }
    }

    public void computeFor(KtSecondaryConstructor ctor) {
        if (ctor != null) {
            ctor.accept(this);
        }
    }
}
