/*
 * Kotlin visitors base - Phase 2
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid;

/**
 * Base class for Kotlin class-level visitors operating on Kotlin PSI (KtClass,
 * KtObjectDeclaration, KtFile, etc.).
 *
 * Mirrors the intent of
 * {@code org.b333vv.metric.model.visitor.type.JavaClassVisitor},
 * but works with Kotlin PSI directly. Uses KtTreeVisitorVoid to ensure
 * recursive traversal of child nodes
 * so overridden visit* methods are invoked for nested elements (functions,
 * properties, statements, etc.).
 */
public abstract class KotlinClassVisitor extends KtTreeVisitorVoid {
    protected Metric metric;

    /**
     * @return last calculated metric, if any
     */
    @Nullable
    public Metric getMetric() {
        return metric;
    }

    /**
     * Entry point to compute metric for a Kotlin class, object or file. Subclasses
     * should override
     * {@link #visitClass(org.jetbrains.kotlin.psi.KtClass)},
     * {@link #visitObjectDeclaration(org.jetbrains.kotlin.psi.KtObjectDeclaration)},
     * or {@link #visitKtFile(org.jetbrains.kotlin.psi.KtFile)}
     * to perform calculation and assign {@link #metric}.
     */
    public void computeFor(KtElement ktElement) {
        if (ktElement != null) {
            ktElement.accept(this);
        }
    }
}
