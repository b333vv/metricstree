package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.util.MethodUtils;
import org.jacoquev.model.metric.value.Value;

public class NumberOfOverriddenMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        long numberOfOverriddenMethods = 0;
        metric.setName("NOOM");
        metric.setDescription("Number Of Overridden Methods");
        metric.setDescriptionUrl("/html/NumberOfOverriddenMethods.html");
        if (ClassUtils.isConcrete(psiClass)) {
            PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                if (!MethodUtils.isConcrete(method)) {
                    continue;
                }
                if (MethodUtils.hasConcreteSuperMethod(method)) {
                    numberOfOverriddenMethods++;
                }
            }
            metric.setValue(Value.of(numberOfOverriddenMethods));
        }
    }
}