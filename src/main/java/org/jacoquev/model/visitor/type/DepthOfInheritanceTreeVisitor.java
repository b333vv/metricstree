package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class DepthOfInheritanceTreeVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("DIT", "Depth Of Inheritance Tree",
                "/html/DepthOfInheritanceTree.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            metric = Metric.of("DIT", "Depth Of Inheritance Tree",
                    "/html/DepthOfInheritanceTree.html", getInheritanceDepth(psiClass));
        }
    }

    private long getInheritanceDepth(PsiClass psiClass) {
        final PsiClass superClass = psiClass.getSuperClass();
        return superClass == null ? 0 : getInheritanceDepth(superClass) + 1;
    }
}