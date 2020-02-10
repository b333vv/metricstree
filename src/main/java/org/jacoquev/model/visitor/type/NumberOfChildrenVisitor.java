package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.jacoquev.model.metric.value.Value;

public class NumberOfChildrenVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric.setName("NOC");
        metric.setDescription("Number Of Children");
        metric.setDescriptionUrl("/html/NumberOfChildren.html");
        if (!(psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                psiClass.isInterface() ||
                psiClass.isEnum()
        )) {
            metric.setValue(Value.of(ClassInheritorsSearch.search(psiClass,
                    false).findAll().size()));
        }
    }
}