/*
 * Kotlin Depth Of Inheritance Tree (DIT) - fully resolved implementation
 */
package org.b333vv.metric.model.visitor.kotlin.type;

import com.intellij.psi.PsiClass;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastContextKt;

import static org.b333vv.metric.model.metric.MetricType.DIT;

/**
 * DIT for Kotlin with full type resolve.
 * Calculates the depth of the inheritance tree by traversing the super classes.
 */
public class KotlinDepthOfInheritanceTreeVisitor extends KotlinClassVisitor {

    @Override
    public void visitClass(@NotNull KtClass klass) {
        int depth = 0;
        UClass uClass = UastContextKt.toUElement(klass, UClass.class);
        if (uClass != null) {
            PsiClass psiClass = uClass.getJavaPsi();
            depth = getInheritanceDepth(psiClass);
        }
        metric = Metric.of(DIT, depth);
    }

    private int getInheritanceDepth(PsiClass psiClass) {
        if (psiClass == null) {
            return 0;
        }
        PsiClass superClass = psiClass.getSuperClass();
        return superClass == null ? 0 : getInheritanceDepth(superClass) + 1;
    }
}
