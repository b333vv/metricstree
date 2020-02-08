package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.visitor.util.MethodComplexityVisitor;

import java.util.Arrays;

public class WeightedMethodCountVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        long weightedMethodCount = 0;

        if (ClassUtils.isConcrete(psiClass)) {
            weightedMethodCount = getWeightedMethodCount(psiClass);
        }

        metric = Metric.of("WMC", "Weighted Method Count",
                "/html/WeightedMethodCount.html", weightedMethodCount);
    }

    private long getWeightedMethodCount(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .mapToLong(this::calculateMethodComplexity)
                .sum();
    }

    public long calculateMethodComplexity(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return 1;
        }
        MethodComplexityVisitor visitor = new MethodComplexityVisitor();
        psiMethod.accept(visitor);
        return visitor.getMethodComplexity();
    }
}