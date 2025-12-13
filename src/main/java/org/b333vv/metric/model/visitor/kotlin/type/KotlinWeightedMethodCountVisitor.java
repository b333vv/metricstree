/*
 * Kotlin Weighted Methods per Class (WMC) - Phase 2.2.1
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinMcCabeCyclomaticComplexityVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.WMC;

/**
 * Computes WMC for a Kotlin class by summing cyclomatic complexities of all
 * functions and constructors declared inside the class, including those in
 * companion objects
 * and nested objects, plus implicit property accessors and data class methods.
 */
public class KotlinWeightedMethodCountVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int sum = 0;

        // Primary constructor: treat as baseline complexity 1
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
            cc.computeFor(primary);
            if (cc.getMetric() != null) {
                sum += (int) cc.getMetric().getPsiValue().longValue();
            }
        }

        // Secondary constructors
        for (KtSecondaryConstructor ctor : klass.getSecondaryConstructors()) {
            KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
            cc.computeFor(ctor);
            if (cc.getMetric() != null) {
                sum += (int) cc.getMetric().getPsiValue().longValue();
            }
        }

        // Member functions in class body
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtNamedFunction) {
                    KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
                    cc.computeFor((KtNamedFunction) decl);
                    if (cc.getMetric() != null) {
                        sum += (int) cc.getMetric().getPsiValue().longValue();
                    }
                } else if (decl instanceof KtProperty) {
                    // Add complexity for property accessors
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

        // Add complexity for data class implicit methods
        // Each implicit method (equals, hashCode, toString, copy, componentN) has
        // complexity 1
        sum += KotlinMetricUtils.countDataClassMethods(klass);

        metric = Metric.of(WMC, sum);
    }

    private int sumForObject(KtObjectDeclaration objectDecl) {
        int sum = 0;
        for (KtDeclaration decl : objectDecl.getDeclarations()) {
            if (decl instanceof KtNamedFunction) {
                KotlinMcCabeCyclomaticComplexityVisitor cc = new KotlinMcCabeCyclomaticComplexityVisitor();
                cc.computeFor((KtNamedFunction) decl);
                if (cc.getMetric() != null) {
                    sum += (int) cc.getMetric().getPsiValue().longValue();
                }
            } else if (decl instanceof KtObjectDeclaration) {
                sum += sumForObject((KtObjectDeclaration) decl);
            }
        }
        return sum;
    }
}
