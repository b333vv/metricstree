package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;

public class NumberOfOperationsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        long numberOfOperations = 0;
        if (ClassUtils.isConcrete(psiClass)) {
            numberOfOperations = psiClass.getAllMethods().length;
        }
        metric = Metric.of("NOO", "Number Of Operations",
                "/html/NumberOfOperations.html", numberOfOperations);
    }
}