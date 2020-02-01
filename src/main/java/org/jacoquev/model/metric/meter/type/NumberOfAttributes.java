package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiClass;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.TypeUtils;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfAttributes implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long numberOfAttributes = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (TypeUtils.isConcrete(psiClass)) {
                result = psiClass.getAllFields().length;
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("NOA", "Number Of Attributes",
                        "/html/NumberOfAttributes.html", numberOfAttributes)
        );
    }
}
