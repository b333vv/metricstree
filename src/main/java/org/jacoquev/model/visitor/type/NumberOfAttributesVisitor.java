package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;

public class NumberOfAttributesVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        int numberOfAttributes = 0;
        if (ClassUtils.isConcrete(psiClass)) {
            numberOfAttributes = psiClass.getAllFields().length;
        }
        metric = Metric.of("NOA", "Number Of Attributes",
                "/html/NumberOfAttributes.html", numberOfAttributes);
    }
}