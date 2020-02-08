package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.MethodUtils;

public class NumberOfOverriddenMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        final PsiMethod[] methods = psiClass.getMethods();
        long numberOfOverriddenMethods = 0;
        for (PsiMethod method : methods) {
            if (!MethodUtils.isConcrete(method)) {
                continue;
            }
            if (MethodUtils.hasConcreteSuperMethod(method)) {
                numberOfOverriddenMethods++;
            }
        }
        metric = Metric.of("NOOM", "Number Of Overridden Methods",
                "/html/NumberOfOverriddenMethods.html", numberOfOverriddenMethods);
    }
}