package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class NumberOfAttributesVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("NOA", "Number Of Attributes",
                "/html/NumberOfAttributes.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            metric = Metric.of("NOA", "Number Of Attributes",
                    "/html/NumberOfAttributes.html", psiClass.getAllFields().length);
        }
    }
}