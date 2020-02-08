package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.jacoquev.model.metric.Metric;

public class NumberOfChildrenVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        long numberOfChildren = 0;
        if (!(psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                psiClass.isInterface() ||
                psiClass.isEnum()
        )) {
            numberOfChildren = ClassInheritorsSearch.search(psiClass, false).findAll().size();
        }
        metric = Metric.of("NOC", "Number Of Children",
                "/html/NumberOfChildren.html", numberOfChildren);
    }
}