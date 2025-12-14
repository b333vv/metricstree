package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;

import static org.b333vv.metric.model.metric.MetricType.NOAM;

/**
 * Kotlin Number Of Added Methods (NOAM)
 * Counts operations added by this class, i.e., methods and accessors defined in
 * this class
 * that do not override a method from a supertype.
 *
 * Includes:
 * - Explicit functions that are not overrides.
 * - Property accessors (explicit or implicit) that are not overrides.
 * - Data class generated methods: copy, componentN (equals/hashCode/toString
 * are overrides).
 * - Private functions/properties (considered added).
 */
public class KotlinNumberOfAddedMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int added = 0;

        // 1. Explicit functions
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtNamedFunction) {
                    KtNamedFunction f = (KtNamedFunction) d;
                    // Ignore overrides
                    if (!f.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                        added++;
                    }
                }
            }
        }

        // 2. Properties (Accessors)
        // Check primary constructor properties
        KtPrimaryConstructor primary = klass.getPrimaryConstructor();
        if (primary != null) {
            for (KtParameter param : primary.getValueParameters()) {
                if (param.hasValOrVar()) {
                    // Check for override
                    if (!param.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                        added += param.isMutable() ? 2 : 1;
                    }
                }
            }
        }

        // Check body properties
        if (body != null) {
            for (KtDeclaration decl : body.getDeclarations()) {
                if (decl instanceof KtProperty) {
                    KtProperty prop = (KtProperty) decl;
                    // Skip companion object properties
                    if (KotlinMetricUtils.isInCompanionObject(prop))
                        continue;

                    if (!prop.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                        added += prop.isVar() ? 2 : 1;
                    }
                }
            }
        }

        // 3. Data class added methods (copy, componentN)
        // equals, hashCode, toString are overrides, so they are not added.
        if (klass.isData()) {
            // copy()
            added++;
            // componentN()
            if (primary != null) {
                added += primary.getValueParameters().size();
            }
        }

        metric = Metric.of(NOAM, added);
    }
}
