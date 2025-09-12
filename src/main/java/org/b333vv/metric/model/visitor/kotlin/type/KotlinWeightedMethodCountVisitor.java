/*
 * Kotlin Weighted Methods per Class (WMC) - Phase 2.2.1
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.method.KotlinMcCabeCyclomaticComplexityVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import static org.b333vv.metric.model.metric.MetricType.WMC;

/**
 * Computes WMC for a Kotlin class by summing cyclomatic complexities of all
 * functions and constructors declared inside the class, including those in companion objects
 * and nested objects.
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
                } else if (decl instanceof KtObjectDeclaration) {
                    // nested object or companion object
                    sum += sumForObject((KtObjectDeclaration) decl);
                }
            }
        }

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
