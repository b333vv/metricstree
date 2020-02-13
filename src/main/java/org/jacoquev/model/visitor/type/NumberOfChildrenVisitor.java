package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Value;

public class NumberOfChildrenVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("NOC", "Number Of Children",
                "/html/NumberOfChildren.html", Value.UNDEFINED);
        if (!(psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                psiClass.isInterface() ||
                psiClass.isEnum()
        )) {
            metric = Metric.of("NOC", "Number Of Children", "/html/NumberOfChildren.html",
                    ClassInheritorsSearch.search(psiClass, false).findAll().size());
        }
    }
}