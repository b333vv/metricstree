package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class NumberOfAttributesAndMethods extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric.setName("SIZE2");
        metric.setDescription("Number of Attributes and Methods");
        metric.setDescriptionUrl("/html/NumberOfAttributesAndMethods.html");
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
            metric.setValue(Value.of(numOperations + numAttributes));
        }
    }
}