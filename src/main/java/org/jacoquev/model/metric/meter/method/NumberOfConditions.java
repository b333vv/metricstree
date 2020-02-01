package org.jacoquev.model.metric.meter.method;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.method.ConditionNestingDepthVisitor;
import org.jacoquev.model.visitor.method.NumberOfConditionsVisitor;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfConditions implements Meter<JavaMethod> {
    @Override
    public Set<Metric> meter(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        long numberOfConditions = MetricsUtils.callInReadAction(() -> getLinesOfCode(psiMethod));
        return ImmutableSet.of(
                Metric.of("NOCdt", "Number Of Conditions",
                        "/html/NumberOfConditions.html", numberOfConditions)
        );
    }

    private long getLinesOfCode(PsiMethod psiMethod) {
        NumberOfConditionsVisitor visitor = new NumberOfConditionsVisitor();
        psiMethod.accept(visitor);
        return visitor.getResult();
    }
}