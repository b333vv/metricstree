package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.b333vv.metricsTree.model.metric.Metric;
import org.b333vv.metricsTree.model.metric.util.ClassUtils;
import org.b333vv.metricsTree.model.metric.value.Value;

public class NumberOfAttributesAndMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("SIZE2", "Number Of Attributes And Methods",
                "/html/NumberOfAttributesAndMethods.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            PsiMethod[] methods = psiClass.getAllMethods();
            PsiField[] fields = psiClass.getAllFields();
            int numOperations = 0;
            for (PsiMethod method : methods) {
                PsiClass containingClass = method.getContainingClass();
                if (containingClass != null && containingClass.equals(psiClass) ||
                        !method.hasModifierProperty(PsiModifier.STATIC)) {
                    numOperations++;
                }
            }
            int numAttributes = 0;
            for (PsiField field : fields) {
                PsiClass containingClass = field.getContainingClass();
                if (containingClass != null && containingClass
                        .equals(psiClass) || !field.hasModifierProperty(PsiModifier.STATIC)) {
                    numAttributes++;
                }
            }
            metric = Metric.of("SIZE2", "Number Of Attributes And Methods",
                    "/html/NumberOfAttributesAndMethods.html", numOperations + numAttributes);
        }
    }
}