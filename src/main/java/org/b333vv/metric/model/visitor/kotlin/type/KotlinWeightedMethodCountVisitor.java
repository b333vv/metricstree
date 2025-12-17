/*
 * Kotlin Weighted Methods per Class (WMC) - Phase 2.2.2
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinMcCabeCyclomaticComplexityVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.WMC;

/**
 * Computes WMC (Weighted Methods per Class) for Kotlin classes.
 * <p>
 * The WMC metric is calculated as the sum of cyclomatic complexities of all methods
 * and method-like constructs in a class. This implementation accounts for Kotlin-specific
 * features and implicit code generation.
 * </p>
 *
 * <h3>Elements included in WMC calculation:</h3>
 * <ul>
 *   <li><b>Primary constructor</b> - baseline complexity of the primary constructor</li>
 *   <li><b>Secondary constructors</b> - complexity of each secondary constructor body</li>
 *   <li><b>Init blocks</b> - complexity of each initialization block</li>
 *   <li><b>Member functions</b> - complexity of all named functions in the class body</li>
 *   <li><b>Property accessors</b> - complexity of getters and setters (both implicit and custom):
 *     <ul>
 *       <li>Standard accessors: val = 1 (getter), var = 2 (getter + setter)</li>
 *       <li>Custom accessors: calculated based on body complexity</li>
 *       <li>Delegated properties: complexity based on delegate implementation</li>
 *     </ul>
 *   </li>
 *   <li><b>Properties from primary constructor</b> - implicit accessors for val/var parameters</li>
 *   <li><b>Nested and companion object members</b> - functions and properties in nested structures</li>
 *   <li><b>Data class implicit methods</b> - equals, hashCode, toString, copy, and componentN functions
 *     (complexity scaled by number of properties for equals/hashCode)</li>
 * </ul>
 *
 * <h3>Exclusions:</h3>
 * <ul>
 *   <li>Abstract functions (no implementation)</li>
 *   <li>External functions</li>
 *   <li>Properties in companion objects are counted separately within the companion object scope</li>
 * </ul>
 *
 * @see KotlinMcCabeCyclomaticComplexityVisitor
 * @see KotlinMetricUtils
 */
public class KotlinWeightedMethodCountVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int sum = 0;

        // Primary constructor: treat as baseline complexity 1
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            sum += getComplexityValue(primary);
        }

        // Secondary constructors
        for (KtSecondaryConstructor ctor : klass.getSecondaryConstructors()) {
            sum += getComplexityValue(ctor);
        }

        // Process class body
        KtClassBody body = klass.getBody();
        if (body != null) {
            // Init blocks (CRITICAL: new functionality)
            for (KtAnonymousInitializer init : body.getAnonymousInitializers()) {
                sum += getComplexityValue(init);
            }

            // Member functions, properties, and nested objects
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    sum += getComplexityValue((KtNamedFunction) decl);
                } else if (decl instanceof KtProperty) {
                    // Add complexity for property accessors (including delegated properties)
                    KtProperty prop = (KtProperty) decl;
                    if (!KotlinMetricUtils.isInCompanionObject(prop)) {
                        sum += KotlinMetricUtils.getAccessorComplexity(prop);
                    }
                } else if (decl instanceof KtObjectDeclaration) {
                    // nested object or companion object
                    sum += sumForObject((KtObjectDeclaration) decl);
                }
            }
        }

        // Add complexity for properties from primary constructor
        if (primary != null) {
            for (KtParameter param : primary.getValueParameters()) {
                if (param.hasValOrVar()) {
                    // Standard accessors: val = 1 (getter), var = 2 (getter + setter)
                    sum += param.isMutable() ? 2 : 1;
                }
            }
        }

        // Add complexity for data class implicit methods (with improved calculation)
        sum += KotlinMetricUtils.countDataClassMethods(klass);

        metric = Metric.of(WMC, sum);
    }

    /**
     * Calculates WMC contribution for nested or companion objects.
     * <p>
     * Includes functions, properties with accessors, and recursively handles nested objects.
     * </p>
     *
     * @param objectDecl the object declaration to analyze
     * @return sum of complexities for all members in the object
     */
    private int sumForObject(KtObjectDeclaration objectDecl) {
        int sum = 0;
        for (KtDeclaration decl : objectDecl.getDeclarations()) {
            if (decl instanceof KtNamedFunction) {
                sum += getComplexityValue((KtNamedFunction) decl);
            } else if (decl instanceof KtProperty) {
                // IMPORTANT: now accounting for properties in companion/nested objects
                sum += KotlinMetricUtils.getAccessorComplexity((KtProperty) decl);
            } else if (decl instanceof KtObjectDeclaration) {
                // Recursively handle nested objects
                sum += sumForObject((KtObjectDeclaration) decl);
            }
        }
        return sum;
    }

    /**
     * Helper method to safely extract complexity value from a visitor for named functions.
     * <p>
     * Reduces code duplication and handles null checks consistently.
     * </p>
     *
     * @param function the function to calculate complexity for
     * @return the complexity value, or 0 if metric is unavailable
     */
    private int getComplexityValue(KtNamedFunction function) {
        KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
        cc.computeFor(function);
        Metric metric = cc.getMetric();
        return metric != null ? (int) metric.getPsiValue().longValue() : 0;
    }

    /**
     * Helper method to safely extract complexity value from a visitor for primary constructors.
     * <p>
     * Reduces code duplication and handles null checks consistently.
     * </p>
     *
     * @param constructor the primary constructor to calculate complexity for
     * @return the complexity value, or 0 if metric is unavailable
     */
    private int getComplexityValue(KtPrimaryConstructor constructor) {
        KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
        cc.computeFor(constructor);
        Metric metric = cc.getMetric();
        return metric != null ? (int) metric.getPsiValue().longValue() : 0;
    }

    /**
     * Helper method to safely extract complexity value from a visitor for secondary constructors.
     * <p>
     * Reduces code duplication and handles null checks consistently.
     * </p>
     *
     * @param constructor the secondary constructor to calculate complexity for
     * @return the complexity value, or 0 if metric is unavailable
     */
    private int getComplexityValue(KtSecondaryConstructor constructor) {
        KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
        cc.computeFor(constructor);
        Metric metric = cc.getMetric();
        return metric != null ? (int) metric.getPsiValue().longValue() : 0;
    }

    /**
     * Helper method to safely extract complexity value from a visitor for init blocks.
     * <p>
     * Reduces code duplication and handles null checks consistently.
     * </p>
     *
     * @param initializer the anonymous initializer (init block) to calculate complexity for
     * @return the complexity value, or 0 if metric is unavailable
     */
    private int getComplexityValue(KtAnonymousInitializer initializer) {
        KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
        cc.computeFor(initializer);
        Metric metric = cc.getMetric();
        return metric != null ? (int) metric.getPsiValue().longValue() : 0;
    }
}
