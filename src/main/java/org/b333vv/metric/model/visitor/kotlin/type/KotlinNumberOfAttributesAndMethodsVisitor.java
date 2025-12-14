package org.b333vv.metric.model.visitor.kotlin.type;

import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;
import com.intellij.psi.PsiElement;

import static org.b333vv.metric.model.metric.MetricType.SIZE2;

/**
 * Kotlin Number Of Attributes And Methods (SIZE2)
 * Counts non-companion properties, declared functions, constructors, and
 * primary constructor properties.
 */
public class KotlinNumberOfAttributesAndMethodsVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int attributes = 0;
        int methods = 0;

        KtPrimaryConstructor primaryConstructor = klass.getPrimaryConstructor();
        if (primaryConstructor != null) {
            methods++;
            for (KtParameter parameter : primaryConstructor.getValueParameters()) {
                if (parameter.hasValOrVar()) {
                    attributes++;
                }
            }
        }

        KtClassBody body = klass.getBody();
        if (body != null) {
            for (KtDeclaration d : body.getDeclarations()) {
                if (d instanceof KtProperty) {
                    if (!isInCompanionObject((KtProperty) d))
                        attributes++;
                } else if (d instanceof KtNamedFunction) {
                    methods++;
                } else if (d instanceof KtSecondaryConstructor) {
                    methods++;
                }
            }
        }
        metric = Metric.of(SIZE2, (long) attributes + methods);
    }

    private boolean isInCompanionObject(@NotNull KtProperty p) {
        PsiElement parent = p.getParent();
        while (parent != null && !(parent instanceof KtClassBody))
            parent = parent.getParent();
        if (parent instanceof KtClassBody) {
            PsiElement maybeCompanion = parent.getParent();
            if (maybeCompanion instanceof KtObjectDeclaration) {
                return ((KtObjectDeclaration) maybeCompanion).isCompanion();
            }
        }
        return false;
    }
}
