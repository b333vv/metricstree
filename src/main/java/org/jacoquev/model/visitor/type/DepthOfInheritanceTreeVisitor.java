package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;

public class DepthOfInheritanceTreeVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        long depthOfInheritanceTree = 0;
        if (ClassUtils.isConcrete(psiClass)) {
            depthOfInheritanceTree = getInheritanceDepth(psiClass);
        }
        metric = Metric.of("DIT", "Depth Of Inheritance Tree",
                        "/html/DepthOfInheritanceTree.html", depthOfInheritanceTree);
    }

    private long getInheritanceDepth(PsiClass psiClass) {
        final PsiClass superClass = psiClass.getSuperClass();
        return superClass == null ? 0 : getInheritanceDepth(superClass) + 1;
    }
}