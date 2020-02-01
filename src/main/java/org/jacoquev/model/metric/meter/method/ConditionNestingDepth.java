package org.jacoquev.model.metric.meter.method;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.method.ConditionNestingDepthVisitor;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class ConditionNestingDepth implements Meter<JavaMethod> {
    @Override
    public Set<Metric> meter(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        long conditionNestingDepth = MetricsUtils.callInReadAction(() -> getLinesOfCode(psiMethod));
        return ImmutableSet.of(
                Metric.of("CND", "Condition Nesting Depth",
                        "/html/ConditionNestingDepth.html", conditionNestingDepth)
        );
    }

    private long getLinesOfCode(PsiMethod psiMethod) {
        ConditionNestingDepthVisitor visitor = new ConditionNestingDepthVisitor();
        psiMethod.accept(visitor);
        return visitor.getResult();
    }
}