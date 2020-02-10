package org.jacoquev.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;
import org.jacoquev.model.visitor.util.MethodComplexityVisitor;

import java.util.Arrays;

public class WeightedMethodCountVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        metric.setName("WMC");
        metric.setDescription("Weighted Method Count");
        metric.setDescriptionUrl("/html/WeightedMethodCount.html");
        if (ClassUtils.isConcrete(psiClass)) {
            metric.setValue(Value.of(getWeightedMethodCount(psiClass)));
        }
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