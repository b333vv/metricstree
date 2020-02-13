package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;

public class NumberOfOperationsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("NOO", "Number Of Operations",
                "/html/NumberOfOperations.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            metric = Metric.of("NOO", "Number Of Operations",
                    "/html/NumberOfOperations.html", psiClass.getAllMethods().length);
        }
    }
}