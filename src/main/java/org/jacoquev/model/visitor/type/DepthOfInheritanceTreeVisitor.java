package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class DepthOfInheritanceTreeVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric.setName("DIT");
        metric.setDescription("Depth Of Inheritance Tree");
        metric.setDescriptionUrl("/html/DepthOfInheritanceTree.html");
        if (ClassUtils.isConcrete(psiClass)) {
            metric.setValue(Value.of(getInheritanceDepth(psiClass)));
        }
    }

    private long getInheritanceDepth(PsiClass psiClass) {
        final PsiClass superClass = psiClass.getSuperClass();
        return superClass == null ? 0 : getInheritanceDepth(superClass) + 1;
    }
}