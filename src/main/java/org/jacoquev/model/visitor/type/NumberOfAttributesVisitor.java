package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class NumberOfAttributesVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric.setName("NOA");
        metric.setDescription("Number Of Attributes");
        metric.setDescriptionUrl("/html/NumberOfAttributes.html");
        if (ClassUtils.isConcrete(psiClass)) {
            metric.setValue(Value.of(psiClass.getAllFields().length));
        }
    }
}