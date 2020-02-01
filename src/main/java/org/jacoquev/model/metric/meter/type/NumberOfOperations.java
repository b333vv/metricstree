package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiClass;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.TypeUtils;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfOperations implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long numberOfOperations = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (TypeUtils.isConcrete(psiClass)) {
                result = psiClass.getAllMethods().length;
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("NOO", "Number Of Operations",
                        "/html/NumberOfOperations.html", numberOfOperations)
        );
    }
}
