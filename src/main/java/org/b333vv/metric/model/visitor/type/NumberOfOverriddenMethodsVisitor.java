package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.MethodUtils;

public class NumberOfOverriddenMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        long overriddenMethodsNumber = 0;
        metric = Metric.of("NOOM", "Number Of Overridden Methods",
                "/html/NumberOfOverriddenMethods.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                if (!MethodUtils.isConcrete(method)) {
                    continue;
                }
                if (MethodUtils.hasConcreteSuperMethod(method)) {
                    overriddenMethodsNumber++;
                }
            }
            metric = Metric.of("NOOM", "Number Of Overridden Methods",
                    "/html/NumberOfOverriddenMethods.html", overriddenMethodsNumber);
        }
    }
}