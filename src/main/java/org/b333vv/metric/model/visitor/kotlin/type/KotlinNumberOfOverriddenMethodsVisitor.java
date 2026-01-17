package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.NOOM;

/**
 * Calculates the Number Of Overridden Methods (NOOM) metric for a Kotlin class.
 * <p>
 * This metric counts all class members that explicitly override inherited
 * members
 * from parent classes or implemented interfaces.
 *
 * <h3>Counted elements:</h3>
 * <ul>
 * <li><b>Overridden functions:</b> Named functions marked with the
 * {@code override} modifier</li>
 * <li><b>Overridden properties:</b> Properties (val/var) marked with the
 * {@code override} modifier</li>
 * </ul>
 *
 * <h3>Not counted:</h3>
 * <ul>
 * <li>Functions and properties without the {@code override} modifier</li>
 * <li>Members declared in nested or inner classes (each class is measured
 * independently)</li>
 * <li>Members declared in companion objects (companion objects are separate
 * types)</li>
 * <li>Abstract function declarations (they don't override, they declare)</li>
 * </ul>
 *
 * <h3>Rationale:</h3>
 * In Kotlin, both functions and properties can be overridden. Unlike Java where
 * only methods
 * can be overridden, Kotlin allows overriding properties, which can have custom
 * accessors.
 * This metric provides insight into:
 * <ul>
 * <li>The degree of polymorphic behavior in the class</li>
 * <li>How much the class customizes inherited behavior</li>
 * <li>Potential coupling to parent classes and interfaces</li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * 
 * <pre>{@code
 * open class Base {
 *     open fun foo() { }
 *     open val bar: String = "base"
 * }
 *
 * class Derived : Base() {
 *     override fun foo() { }        // Counted (NOOM += 1)
 *     override val bar = "derived"  // Counted (NOOM += 1)
 *     fun baz() { }                 // Not counted (no override modifier)
 * }
 * // Result: NOOM = 2
 * }</pre>
 *
 * <h3>Metric interpretation:</h3>
 * <ul>
 * <li><b>NOOM = 0:</b> The class doesn't override any inherited members (base
 * class or final class)</li>
 * <li><b>Low NOOM (1-3):</b> Minimal customization of inherited behavior</li>
 * <li><b>High NOOM (>10):</b> Extensive overriding may indicate:
 * <ul>
 * <li>Strong coupling to parent types</li>
 * <li>Adapter pattern implementation</li>
 * <li>Potential design issues if combined with high complexity metrics</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @see org.b333vv.metric.model.metric.MetricType#NOOM
 * @see KotlinNumberOfMethodsVisitor
 * @see KotlinNumberOfAddedMethodsVisitor
 */
public class KotlinNumberOfOverriddenMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        compute(klass);
    }

    @Override
    public void visitObjectDeclaration(@NotNull KtObjectDeclaration declaration) {
        compute(declaration);
    }

    @Override
    public void visitKtFile(@NotNull KtFile file) {
        compute(file);
    }

    private void compute(@NotNull KtElement element) {
        java.util.List<KtDeclaration> declarations = java.util.Collections.emptyList();
        if (element instanceof KtClassOrObject) {
            KtClassBody body = ((KtClassOrObject) element).getBody();
            if (body != null) {
                declarations = body.getDeclarations();
            }
        } else if (element instanceof KtFile) {
            declarations = ((KtFile) element).getDeclarations();
        }

        int overridden = countOverriddenMembers(declarations);
        metric = Metric.of(NOOM, overridden);
    }

    /**
     * Counts all overridden members (functions and properties) directly declared
     * in the class body.
     * <p>
     * This method only processes declarations that are direct children of the
     * class.
     * Nested classes, companion objects, and other nested declarations are excluded
     * as they represent separate types with their own metrics.
     *
     * @param body the class body to analyze, may be null for classes without a body
     * @return count of overridden members (functions + properties), 0 if body is
     *         null
     */
    private int countOverriddenMembers(@NotNull java.util.List<KtDeclaration> declarations) {
        int count = 0;
        for (KtDeclaration declaration : declarations) {
            if (declaration instanceof KtNamedFunction) {
                // Count overridden functions
                KtNamedFunction function = (KtNamedFunction) declaration;
                if (function.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                    count++;
                }
            } else if (declaration instanceof KtProperty) {
                // Count overridden properties (val/var)
                // In Kotlin, properties can override properties from parent classes/interfaces
                KtProperty property = (KtProperty) declaration;
                if (property.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                    count++;
                }
            }
            // Note: We deliberately skip KtClass and KtObjectDeclaration to avoid
            // counting nested types, as they should be measured independently
        }

        return count;
    }
}
