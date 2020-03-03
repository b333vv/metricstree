package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.b333vv.metricsTree.model.metric.util.ClassUtils;
import org.b333vv.metricsTree.model.metric.value.Value;
import org.b333vv.metricsTree.model.metric.Metric;

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