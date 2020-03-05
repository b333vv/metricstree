package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

public class NumberOfAttributesAndMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("SIZE2", "Number Of Attributes And Methods",
                "/html/NumberOfAttributesAndMethods.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            PsiMethod[] methods = psiClass.getAllMethods();
            PsiField[] fields = psiClass.getAllFields();
            int operationsNumber = 0;
            for (PsiMethod method : methods) {
                PsiClass containingClass = method.getContainingClass();
                if (containingClass != null && containingClass.equals(psiClass) ||
                        !method.hasModifierProperty(PsiModifier.STATIC)) {
                    operationsNumber++;
                }
            }
            int attributesNumber = 0;
            for (PsiField field : fields) {
                PsiClass containingClass = field.getContainingClass();
                if (containingClass != null && containingClass
                        .equals(psiClass) || !field.hasModifierProperty(PsiModifier.STATIC)) {
                    attributesNumber++;
                }
            }
            metric = Metric.of("SIZE2", "Number Of Attributes And Methods",
                    "/html/NumberOfAttributesAndMethods.html", operationsNumber + attributesNumber);
        }
    }
}