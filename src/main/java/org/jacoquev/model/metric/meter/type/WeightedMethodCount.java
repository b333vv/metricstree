package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiTypeParameter;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.meter.CyclomaticComplexity;
import org.jacoquev.util.MetricsUtils;

import java.util.Arrays;
import java.util.Set;

public class WeightedMethodCount implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long weightedMethodCount = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (!(psiClass.isInterface() ||
                    psiClass.isEnum() ||
                    psiClass instanceof PsiAnonymousClass ||
                    psiClass instanceof PsiTypeParameter ||
                    psiClass.getParent() instanceof PsiDeclarationStatement)) {
                result = getWeightedMethodCount(psiClass);
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("WMC", "Weighted Method Count",
                        "/html/WeightedMethodCount.html", weightedMethodCount)
        );
    }

    private long getWeightedMethodCount(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .mapToLong(CyclomaticComplexity::calculate)
                .sum();
    }
}
