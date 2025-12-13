package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.visitor.kotlin.KotlinMetricUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import com.intellij.psi.PsiElement;

import static org.b333vv.metric.model.metric.MetricType.NOPA;

/**
 * Kotlin Number Of Public Attributes (NOPA)
 * Counts properties declared in the class body that are effectively public
 * instance attributes.
 * Heuristics:
 * - Excludes properties inside companion objects (static-like) and those with
 * 'const' modifier.
 * - Treats properties without 'private' or 'protected' as public.
 */
public class KotlinNumberOfPublicAttributesVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int publicAttrs = 0;
        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtProperty) {
                    KtProperty p = (KtProperty) d;
                    if (isInCompanionObject(p))
                        continue;
                    if (p.hasModifier(KtTokens.CONST_KEYWORD))
                        continue;
                    // Use KotlinMetricUtils to check if truly public (not private, protected, or
                    // internal)
                    if (KotlinMetricUtils.isPublicProperty(p)) {
                        publicAttrs++;
                    }
                }
            }
        }
        metric = Metric.of(NOPA, publicAttrs);
    }

    private boolean isInCompanionObject(@NotNull KtProperty p) {
        PsiElement parent = p.getParent();
        while (parent != null && !(parent instanceof KtClassBody))
            parent = parent.getParent();
        if (parent instanceof KtClassBody) {
            PsiElement maybeCompanion = parent.getParent();
            if (maybeCompanion instanceof KtObjectDeclaration) {
                KtObjectDeclaration obj = (KtObjectDeclaration) maybeCompanion;
                return obj.isCompanion();
            }
        }
        return false;
    }
}
