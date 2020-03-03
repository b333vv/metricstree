package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;

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